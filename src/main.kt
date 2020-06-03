import test.TestHelper
import java.io.File
import java.io.PrintStream


fun main(args: Array<String>) {
    /*var query = "SELECT f.*, ((SELECT sum(a.flight_id) FROM flights AS a GROUP BY a.flight_id )) AS s\n" +
            "FROM flights AS f\n" +
            "\tINNER JOIN aircrafts AS air ON ((air.aircraft_code=f.aircraft_code and air.aircraft_code=f.aircraft_code) or ((1=1 and 1=1) or ((2>3 and 4<2) or (3!=3 and 2<1))))\n" +
            "\tFULL OUTER JOIN aircrafts_data AS ad ON ad.aircraft_code=air.aircraft_code\n" +
            "\tLEFT JOIN airports_data AS apd ON apd.airport_code=f.aircraft_code\n" +
            "WHERE (f.flight_no='a' and (((f.aircraft_code='asd' or f.aircraft_code='5454') and f.aircraft_code='2332') or (SELECT count(*)+((SELECT count(*) FROM flights )) FROM tickets )>10))\n" +
            "ORDER BY f.aircraft_code, f.flight_no, ((SELECT max(airports.airport_code) FROM airports LIMIT 10 )) DESC\n" +
            "LIMIT ((SELECT count(*) FROM airports ))"*/

    /*var query = "select data.city, count(*)\n" +
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
    "group by data.city"*/

    /*var query = "select    distinct    d.*\n" +
            "from  (   select            distinct * from aircrafts) as   d\n"*/

    /*var query = "select f.aircraft_code as distinct\n" +
            "from flights as f\n" +
            "    join aircrafts as a on not a.aircraft_code = f.aircraft_code\n" +
            "where not (f.flight_id > 10 and f.flight_id < 20) and f.flight_id not between (((select 11))) and (select 20) and f.flight_id in (1,  3, 4, (select distinct ff.flight_id from flights as ff limit 1)) and f.aircraft_code  like   '%4343%'\n" +
            "limit 10"*/


    /*
    var query = "select f.aircraft_code as join\n" +
            "from flights as f\n" +
            "group by f.aircraft_code\n" +
            "having count(*) between (10) and (200) and count(*) in (1,2,3,4,5,6,7,8)"

    query = QueryParseHelper.convertQueryToLine(query)
    var normalizedQuery = QueryParseHelper.normalizeQueryString(query)

    var subQueriesBounds = QueryParseHelper.findQueriesBounds(normalizedQuery)
    var tree = QueryTree(normalizedQuery, subQueriesBounds)

    tree.parse()
    tree.print()*/

    // Creating a File object that represents the disk file.

    // Creating a File object that represents the disk file.
    val o = PrintStream(File("A.txt"))
    System.setOut(o)

    var data = TestHelper.loadData("testData.txt")
    TestHelper.testQueries(data, "log.txt")
}