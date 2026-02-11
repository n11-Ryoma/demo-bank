package com.example.ebank.publicinfo.repository.jdbc;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.example.ebank.publicinfo.dto.AtmLocation;

@Repository
public class AtmRepositoryJdbc {

    private final JdbcTemplate jdbc;

    public AtmRepositoryJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public SearchResult search(String pref, boolean openNow, boolean cash, String q,
                               String service, String sort, String order, int page, int size) {
        StringBuilder where = new StringBuilder(" where 1=1");
        List<Object> params = new ArrayList<>();

        if (pref != null && !pref.isBlank()) {
            where.append(" and lower(pref) = lower(?)");
            params.add(pref);
        }
        if (openNow) {
            where.append(" and open_now = true");
        }
        if (cash) {
            where.append(" and cash = true");
        }
        if (q != null && !q.isBlank()) {
            where.append(" and (lower(name) like ? or lower(address) like ? or lower(city) like ? or lower(pref) like ?)");
            String like = like(q);
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (service != null && !service.isBlank()) {
            where.append(" and lower(services) like ?");
            params.add("%" + service.toLowerCase(Locale.ROOT) + "%");
        }

        String orderBy = "name";
        if (sort != null && !sort.isBlank()) {
            switch (sort.toLowerCase(Locale.ROOT)) {
                case "updated":
                    orderBy = "updated_at";
                    break;
                case "city":
                    orderBy = "city";
                    break;
                case "name":
                default:
                    orderBy = "name";
                    break;
            }
        }

        String direction = "asc";
        if (order != null && order.equalsIgnoreCase("desc")) {
            direction = "desc";
        }

        long total = jdbc.queryForObject(
                "select count(*) from atm_locations" + where,
                params.toArray(),
                Long.class);

        String sql = "select id, name, pref, city, address, lat, lng, open_now, cash, services, hours, updated_at " +
                "from atm_locations" + where + " order by " + orderBy + " " + direction + " limit ? offset ?";

        List<Object> listParams = new ArrayList<>(params);
        listParams.add(size);
        listParams.add((page - 1) * size);

        List<AtmLocation> items = jdbc.query(sql, listParams.toArray(), atmRowMapper());
        return new SearchResult(items, total);
    }

    private RowMapper<AtmLocation> atmRowMapper() {
        return (rs, rowNum) -> {
            String services = rs.getString("services");
            List<String> serviceList = splitCsv(services);
            OffsetDateTime updatedAt = rs.getObject("updated_at", OffsetDateTime.class);
            return new AtmLocation(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getString("pref"),
                    rs.getString("city"),
                    rs.getString("address"),
                    rs.getDouble("lat"),
                    rs.getDouble("lng"),
                    rs.getBoolean("open_now"),
                    rs.getBoolean("cash"),
                    serviceList,
                    rs.getString("hours"),
                    null,
                    updatedAt == null ? null : updatedAt.toString());
        };
    }

    private List<String> splitCsv(String csv) {
        List<String> list = new ArrayList<>();
        if (csv == null || csv.isBlank()) {
            return list;
        }
        for (String part : csv.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isBlank()) {
                list.add(trimmed);
            }
        }
        return list;
    }

    private String like(String q) {
        return "%" + q.toLowerCase(Locale.ROOT) + "%";
    }

    public static class SearchResult {
        private final List<AtmLocation> items;
        private final long total;

        public SearchResult(List<AtmLocation> items, long total) {
            this.items = items;
            this.total = total;
        }

        public List<AtmLocation> getItems() {
            return items;
        }

        public long getTotal() {
            return total;
        }
    }
}
