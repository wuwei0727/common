//package com.tgy.rtls.web.config;
//
//import com.google.common.collect.Range;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
//import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
//import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.*;
//
//@Slf4j
//public class MonthStrategyShardingAlgorithm implements StandardShardingAlgorithm<LocalDateTime> {
//
//    /** 配置值需要储存 */
//    private Properties props;
//
//    private static final DateTimeFormatter yyyyMM = DateTimeFormatter.ofPattern("yyyyMM");
//
//    @Override
//    public String doSharding(Collection<String> collection, PreciseShardingValue<LocalDateTime> preciseShardingValue) {
//        LocalDateTime dateTime = preciseShardingValue.getValue();
//        String tableSuffix = dateTime.format(yyyyMM);
//        String logicTableName = preciseShardingValue.getLogicTableName();
//        String table = logicTableName.concat("_").concat(tableSuffix);
//        try {
//            return collection.stream().filter(s -> s.equals(table)).findFirst().<RuntimeException>orElseThrow(() -> {
//                log.error("逻辑分表不存在");
//                return null;
//            });
//        } catch (RuntimeException e) {
//            throw new RuntimeException("逻辑分表不存在");
//        }
//    }
//
//    @Override
//    public Collection<String> doSharding(Collection<String> collection, RangeShardingValue<LocalDateTime> rangeShardingValue) {
//        // 逻辑表名
//        String logicTableName = rangeShardingValue.getLogicTableName();
//
//        // between and 的起始值
//        Range<LocalDateTime> valueRange = rangeShardingValue.getValueRange();
//
//        Set<String> queryRangeTables = extracted(logicTableName, valueRange.lowerEndpoint(), valueRange.upperEndpoint());
//        ArrayList<String> tables = new ArrayList<>(collection);
//        tables.retainAll(queryRangeTables);
//        System.out.println("MonthStrategyShardingAlgorithm.doSharding tables collection name: {}" + tables);
//        return tables;
//    }
//
//
//    /**
//     * 根据范围计算表明
//     *
//     * @param logicTableName 逻辑表明
//     * @param lowerEndpoint  范围起点
//     * @param upperEndpoint  范围终端
//     * @return 物理表名集合
//     */
//    private Set<String> extracted(String logicTableName, LocalDateTime lowerEndpoint, LocalDateTime upperEndpoint) {
//        Set<String> rangeTable = new HashSet<>();
//        while (lowerEndpoint.isBefore(upperEndpoint)) {
//            String str = getTableNameByDate(lowerEndpoint, logicTableName);
//            rangeTable.add(str);
//            lowerEndpoint = lowerEndpoint.plusMonths(1);
//        }
//        // 获取物理表明
//        String tableName = getTableNameByDate(upperEndpoint, logicTableName);
//        rangeTable.add(tableName);
//        return rangeTable;
//    }
//
//    /**
//     * 根据日期获取表明
//     *
//     * @param dateTime       日期
//     * @param logicTableName 逻辑表名
//     * @return 物理表名
//     */
//    private String getTableNameByDate(LocalDateTime dateTime, String logicTableName) {
//        String tableSuffix = dateTime.format(yyyyMM);
//        return logicTableName.concat("_").concat(tableSuffix);
//    }
//
//    @Override
//    public Properties getProps() {
//        return props;
//    }
//
////    @Override
////    public void init(Properties properties) {
////        this.props = properties;
////    }
//
//    @Override
//    public String getType() {
//        return "CREATE_TIME";
//    }
//
//    @Override
//    public void init() {
//
//    }
//}
//
