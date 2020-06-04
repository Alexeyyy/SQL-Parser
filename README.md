<h1>SQL-Parser</h1>
<p>
Парсер, который позволяет "разобрать" SQL запрос вида SELECT по "полочкам".
</p>
<p>
Например, вот такие запросы (несовсем логичные примеры с точки зрения смысла, но синтаксически верные --> значит имеют место быть):
</p>

```SQL
Запрос 1

select     f.*,           (select     sum   (           a.flight_id)from flights              as a group by a.flight_id) as s         from flights as f
    join           aircrafts   as air on ((((air.aircraft_code) = f.aircraft_code)) and air.aircraft_code = f.aircraft_code) or (1=1 and 1=1 or (((2 >   3 and 4< 2 or 3!=3 and 2< 1))))
    full       outer   join   aircrafts_data as ad on ad.aircraft_code = air.aircraft_code
    left  join          airports_data     as    apd on    apd.airport_code = f.aircraft_code and apd.airport_code =      (select ap.airport_code from airports   as   ap   where   (select     1 as   column) = 1   limit  1   )
where   f.flight_no =   'a'    and   ((f.aircraft_code = 'asd' or    f.aircraft_code    = '5454'    ) and      f.aircraft_code = '2332' or (select count(*)   + (select count(*)    from flights) from tickets)     > 10)
order  by f.aircraft_code,    f.flight_no, (select max(airports.airport_code) from airports limit 10) asc
limit  ( select    count(*)     from        airports    )

Запрос 2

SeLeCt "f1".flight_id
fRoM flights as "f1"
    inner join flights as f_2 on "f1".aircraft_code = f_2.aircraft_code
where "f1".flight_id between 10 and 100 and f_2.flight_id not in (1,2,3,4,5,6,7, (select 1), (select (select 2)))
group by "f1".flight_id
order by "f1".flight_id DESC
limit ((select count(*) alias_1 from flights limit all) + (select count(*) + 1 alias_2 from flights limit null))
offset (select 8)

Запрос 3

select (select (select (select (select (select (select (select (select (select 1 a)b)c)d)e)f)g)h)i)k
```

<p>
P.S. схема и данные PG-базы взяты с сайта <a href="https://postgrespro.ru/education/demodb">PostgresPro</a>, за что им отдельное Спасибо!
</p>

<h2>Как это работает?</h2>

<p>
Если не вдаваться в подробности, то парсер работает по следующему алгоритму:
</p>
<ul>
<li>"нормирует" строку запроса с точки зрения удаления лишних пробелов</li>
<li>находит все подзапросы и составляет "дерево" из них</li>
<li>раскладывает каждый запрос по ключевым clause (select, from, join, where и т.д.)</li>
<li>парсит каждый из запросов в отдельности</li>
<li>печатает информацию в виде таблиц в консоли (Таблица 1)</li>
</ul>

<p>
Таблица 1. Пример разбора select-строки запроса.
</p>

```
ТУТ ПРИМЕР
```

<p>
  Дополнительно реализован функционал восстановления запроса – обратная операция, чтобы была возможность потестировать правильность раскладки строк запроса парсером.
</p>

<h2>P.S.</h2>
<p>
  В парсер не встроена проверка на валидность SQL-строки, поэтому <strong>пожалуйста</strong> подавайте запрос, который компилируется утилитой psql или какой-нибудь средой, например, DataGrip.
</p>
