import helpers.QueryParseHelper
import models.QueryTree

fun main(args: Array<String>) {
    //val query = "select f.aircraft_code, count(f.flight_id), sum(b.total_amount) from flights as f join ticket_flights as tf on tf.flight_id = f.flight_id     join tickets as t on t.ticket_no = tf.ticket_no    join bookings as b on t.book_ref = b.book_ref group by f.aircraft_code having count(f.flight_id) > 20000 or sum(b.total_amount) < 20000000000"
    //var query = "select     f.*, (select sum(a.flight_id)from flights              as a group by a.flight_id) as s         from flights as f     join           aircrafts   as air on ((((air.aircraft_code) = f.aircraft_code)) and air.aircraft_code = f.aircraft_code) or (1=1 and 1=1 or (((2 >   3 and 4< 2 or 3!=3 and 2< 1))))     full       outer   join   aircrafts_data as ad on ad.aircraft_code = air.aircraft_code     left  join          airports_data     as    apd on    apd.airport_code = f.aircraft_code and apd.airport_code =      (select ap.airport_code from airports   as   ap   where   (select     1 as   column) = 1   limit  1   ) where   f.flight_no =   'a'    and   ((f.aircraft_code = 'asd' or    f.aircraft_code    = '5454'    ) and      f.aircraft_code = '2332' or (select count(*)   + (select count(*)    from flights) from tickets)     > 10) order  by f.aircraft_code,    f.flight_no, (select max(airports.airport_code) from airports limit 10) DESC LIMIT  ( select    count(*)     from        airports    )"
    //var query = "select * from table as t group by t.a having sum(((t.cost))) > 1000 and count(*) < 100 where t.name like '%4343%'")
    //var query = "select f.aircraft_code, count(f.flight_id), sum(b.total_amount) from flights as f     join ticket_flights as tf on tf.flight_id = f.flight_id    join tickets as t on t.ticket_no = tf.ticket_no    join bookings as b on t.book_ref = b.book_ref where f.flight_id  not    like ((('%4323323 23232%')))      group by f.aircraft_code having count((f.flight_id)) > 20000 and not (sum(b.total_amount) <> 20000000000)"
    //var query = "SELECT f.aircraft_code, count(f.flight_id), sum(b.total_amount)FROM flights AS f INNER JOIN ticket_flights AS tf ON tf.flight_id=f.flight_id FULL OUTER JOIN tickets AS t ON t.ticket_no=tf.ticket_no  INNER JOIN bookings AS b ON t.book_ref=b.book_ref WHERE f.aircraft_code not like ((('%432332323232%')))GROUP BY f.aircraft_code HAVING not (sum(b.total_amount)<>20000000000) and count(f.flight_id)>20000"

    /*var query = "SELECT f.*, ((SELECT sum(a.flight_id) FROM flights AS a GROUP BY a.flight_id )) AS s\n" +
            "FROM flights AS f\n" +
            "\tINNER JOIN aircrafts AS air ON ((air.aircraft_code=f.aircraft_code and air.aircraft_code=f.aircraft_code) or ((1=1 and 1=1) or ((2>3 and 4<2) or (3!=3 and 2<1))))\n" +
            "\tFULL OUTER JOIN aircrafts_data AS ad ON ad.aircraft_code=air.aircraft_code\n" +
            "\tLEFT JOIN airports_data AS apd ON apd.airport_code=f.aircraft_code\n" +
            "WHERE (f.flight_no='a' and (((f.aircraft_code='asd' or f.aircraft_code='5454') and f.aircraft_code='2332') or (SELECT count(*)+((SELECT count(*) FROM flights )) FROM tickets )>10))\n" +
            "ORDER BY f.aircraft_code, f.flight_no, ((SELECT max(airports.airport_code) FROM airports LIMIT 10 )) DESC\n" +
            "LIMIT ((SELECT count(*) FROM airports ))"*/
    var query = "select data.city, count(*)\n" +
            "from (\n" +
            "    select big_air_landings.city, a.airport_name, a.airport_code\n" +
            "    from (\n" +
            "             select ap.city as city, count(*) as ap_count\n" +
            "             from airports as ap\n" +
            "             group by ap.city\n" +
            "             having count(*) > 1\n" +
            "         ) as big_air_landings\n" +
            "        join airports as a on a.city = big_air_landings.city\n" +
            ") as data\n" +
            "where (1=1 or 2=2 and 3=3 or (5=5 and 6=6 and ((7=7 or (18-5 = 13)))) and (((select 1 limit 1) = 1) and 4 between 1 and 4)) and data.city like '%лья%'\n" +
            "group by data.city"


    query = QueryParseHelper.convertQueryToLine(query)
    var normalizedQuery = QueryParseHelper.normalizeQueryString(query)
    var subQueriesBounds = QueryParseHelper.findQueriesBounds(normalizedQuery)
    var tree = QueryTree(normalizedQuery, subQueriesBounds)

    tree.parse()
    tree.print()
}