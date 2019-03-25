package com.atguigu.gmall.list.servlce.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.SkuLsInfo;
import com.atguigu.gmall.bean.SkuLsParams;
import com.atguigu.gmall.bean.SkuLsResult;
import com.atguigu.gmall.config.RedisUtil;
import com.atguigu.gmall.service.ListService;
import io.searchbox.client.JestClient;
import io.searchbox.core.*;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author York
 * @create 2018-11-11 19:56
 */
@Service
public class ListServiceImpl implements ListService {

    @Autowired
    JestClient jestClient;

    @Autowired
    RedisUtil redisUtil;

    public static final String ES_INDEX="gmall";

    public static final String ES_TYPE="SkuInfo";

    @Override
    public void saveSkuInfo(SkuLsInfo skuLsInfo) {
        //保存数据
        Index index =
                new Index.Builder(skuLsInfo)
                        .index(ES_INDEX).type(ES_TYPE)
                        .id(skuLsInfo.getId()).build();
        try {
            DocumentResult documentResult = jestClient.execute(index);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    @Override
    public SkuLsResult search(SkuLsParams skuLsParams) {
        // 编写dsl 语句
        String query = makeQueryStringForSearch(skuLsParams);
        // 执行
        Search search = new Search.Builder(query).addIndex(ES_INDEX).addType(ES_TYPE).build();

        SearchResult searchResult = null;
        try {
            searchResult = jestClient.execute(search);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // searchResult 转化为 SkuLsResult
        SkuLsResult skuLsResult = makeResultForSearch(skuLsParams, searchResult);
        // 返回
        return skuLsResult;
    }

    @Override
    public void incrHotScore(String skuId) {
        Jedis jedis = redisUtil.getJedis();
        int timesToEs = 10;
        //记录次数
        Double count = jedis.zincrby("hotScore",1,"skuId"+skuId);
        if (count%timesToEs==0){
            updateHotScore(skuId,Math.round(count));

        }
    }

    private void updateHotScore(String skuId,Long hotScore){
        String updateJson="{\n" +
                "   \"doc\":{\n" +
                "     \"hotScore\":"+hotScore+"\n" +
                "   }\n" +
                "}";

        Update update = new Update.Builder(updateJson).index("gmall").type("SkuInfo").id(skuId).build();
        try {
            jestClient.execute(update);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    /**
     * 编写dsl语句
     * @param skuLsParams
     * @return
     */
    private String makeQueryStringForSearch(SkuLsParams skuLsParams){
        //创建一个查询器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //创建bool
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //keyword存在于传入的对象中,判断keyword是否有值
        if (skuLsParams.getKeyword()!=null && skuLsParams.getKeyword().length()>0){
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName", skuLsParams.getKeyword());
            //将matchQueryBuilder放入must中
            boolQueryBuilder.must(matchQueryBuilder);
            //设置高亮
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            //设置要高亮显示的字段
            highlightBuilder.field("skuName");
            //设置高亮显示的格式
            highlightBuilder.postTags("</span>");
            highlightBuilder.preTags("<span style='color:red'>");
            //将设置好的高亮放入查询器
            searchSourceBuilder.highlight(highlightBuilder);

        }
        //catalog3Id
        if (skuLsParams.getCatalog3Id()!=null&&skuLsParams.getCatalog3Id().length()>0){
            //term
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id", skuLsParams.getCatalog3Id());
            //term放入filter中
            boolQueryBuilder.filter(termQueryBuilder);
        }
        //平台属性值Id
        if (skuLsParams.getValueId()!=null&&skuLsParams.getValueId().length>0){
            //循环将平台属性值放入term中
            for (int i = 0; i < skuLsParams.getValueId().length; i++) {
                //获取其中的每一个平台属性值得id
                String valueId = skuLsParams.getValueId()[i];
                TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId", valueId);
                //将term放入filter中
                boolQueryBuilder.filter(termQueryBuilder);
            }

        }
        searchSourceBuilder.query(boolQueryBuilder);

        // 设置分页 from : 表示从哪开始查询每页显示的数据
        // (pageNo-1)*pageSize
        int from = (skuLsParams.getPageNo() - 1) * skuLsParams.getPageSize();
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(skuLsParams.getPageSize());

        //设置排序
        searchSourceBuilder.sort("hotScore",SortOrder.DESC);
        //聚合
        TermsBuilder groupby_attr = AggregationBuilders.terms("groupby_attr").field("skuAttrValueList.valueId");
        searchSourceBuilder.aggregation(groupby_attr);
        //将构建好的query语句转换为string
        String query = searchSourceBuilder.toString();
        return query;
    }

    /**
     * 对象转换
     * @param skuLsParams
     * @param searchResult
     * @return
     */
    private SkuLsResult makeResultForSearch(SkuLsParams skuLsParams,SearchResult searchResult){
        //searchResult转化为SkuLsResult
        SkuLsResult skuLsResult = new SkuLsResult();
        // 创建一个空的集合，该集合中应该是通过dsl 语句查询的searchResult 中的SkuLsInfo
        ArrayList<SkuLsInfo> arrayList = new ArrayList<>();

        //取得searchResult中的数据
        List<SearchResult.Hit<SkuLsInfo, Void>> hits = searchResult.getHits(SkuLsInfo.class);
        //循环取得数据
        for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
            SkuLsInfo skuLsInfo = hit.source;
            //对skuName进行高亮显示
            if (hit.highlight!=null&&hit.highlight.size()>0){
                Map<String,List<String>>highlight = hit.highlight;
                List<String> list = highlight.get("skuName");
                //取出高亮的名称
                String skuNameHl = list.get(0);
                //将skuLsInfo的skuName替换为高亮的skuName
                skuLsInfo.setSkuName(skuNameHl);
            }
            arrayList.add(skuLsInfo);
        }
        // 将searchResult 中的skuLsInfo 集合添加到 skuLsResult
        skuLsResult.setSkuLsInfoList(arrayList);
        //设置总条数
        skuLsResult.setTotal(searchResult.getTotal());
        //设置总页数
        long page = (searchResult.getTotal() + skuLsParams.getPageSize() -1) / skuLsParams.getPageSize();

        skuLsResult.setTotalPages(page);
        //设置平台属性值id
        ArrayList<String >stringArrayList = new ArrayList<>();
        //平台属性值的Id通过SearchResult的聚合获得
        MetricAggregation aggregations = searchResult.getAggregations();
        TermsAggregation groupby_attr = aggregations.getTermsAggregation("groupby_attr");
        List<TermsAggregation.Entry> buckets = groupby_attr.getBuckets();
        if (buckets!=null && buckets.size()>0){
            for (TermsAggregation.Entry bucket : buckets) {
                // getKey() 平台属性值的Id
                String valueId = bucket.getKey();
                stringArrayList.add(valueId);
            }
        }
        skuLsResult.setAttrValueIdList(stringArrayList);
        return skuLsResult;

    }

}
