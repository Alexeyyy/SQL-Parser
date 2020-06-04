<h1>SQL-Parser</h1>
<p>
Парсер, который позволяет "разобрать" SQL запрос вида SELECT по "полочкам".
</p>
<p>
Например, вот такие запросы (несовсем логичные примеры с точки зрения смысла, но синтаксически верные --> значит имеют место быть):
</p>

<h4>Запрос 1</h4>
```SQL
select     f.*,           (select     sum   (           a.flight_id)from flights              as a group by a.flight_id) as s         from flights as f
    join           aircrafts   as air on ((((air.aircraft_code) = f.aircraft_code)) and air.aircraft_code = f.aircraft_code) or (1=1 and 1=1 or (((2 >   3 and 4< 2 or 3!=3 and 2< 1))))
    full       outer   join   aircrafts_data as ad on ad.aircraft_code = air.aircraft_code
    left  join          airports_data     as    apd on    apd.airport_code = f.aircraft_code and apd.airport_code =      (select ap.airport_code from airports   as   ap   where   (select     1 as   column) = 1   limit  1   )
where   f.flight_no =   'a'    and   ((f.aircraft_code = 'asd' or    f.aircraft_code    = '5454'    ) and      f.aircraft_code = '2332' or (select count(*)   + (select count(*)    from flights) from tickets)     > 10)
order  by f.aircraft_code,    f.flight_no, (select max(airports.airport_code) from airports limit 10) asc
limit  ( select    count(*)     from        airports    )
```

<p>
P.S. схема и данные PG-базы взяты с сайта <a href="https://postgrespro.ru/education/demodb">PostgresPro</a>, за что им отдельное Спасибо!
</p>

<h2>Как работает парсер?</h2>

<p>
Если не вдаваться в подробности, то парсер работает по следующему алгоритму:
</p>
<ul>
<li>Набор запросов, которые необходимо "распарсить". Вот пример <a href="https://github.com/Alexeyyy/SQL-Parser/blob/master/testData.txt">тестового файла</a>.</li>
<li>"Нормирует" строку запроса с точки зрения удаления лишних пробелов</li>
<li>Находит все подзапросы и составляет "дерево вложенности". Для i-го запроса все видимые подзапросы маркируются guid-ами</li>
<li>Раскладывает запрос и все его подзапросы по ключевым словам - clauses, ими являются select, from, join, where, order by, group by, having, limit, offset</li>
<li>Парсит каждый из запросов в отдельности и сохраняет данные в виде списков</li>
<li>Печатает информацию в виде таблиц в файл. Пример <a href="https://github.com/Alexeyyy/SQL-Parser/blob/master/result.txt">выходных данных</a> программы-парсера</li>
</ul>

<p>
  Дополнительно был реализован функционал восстановления запроса – обратная операция, чтобы была возможность протестировать правильность раскладки частей запроса парсером.
</p>

<h4>P.S.</h4>
<p>
  В парсер не встроена проверка на синтаксическую правильность SQL (компилятора тут нет =) ), поэтому <strong>пожалуйста</strong> подавайте запрос, который компилируется утилитой psql или какой-нибудь средой, например, DataGrip.
</p>

<h4>P.P.S.</h4>
<p>
    Недостатком парсера является то, что он не поддерживает алиасы в виде ключевых слов SQL (могут быть интересные последствия), например:
</p>
```SQL
SELECT 10 AS JOIN
```
