package com.ntuzy;

import com.alibaba.fastjson.JSON;
import com.ntuzy.pojo.User;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * es 7.6.x api
 */
@SpringBootTest
class NtuzyEsApiApplicationTests {


    // 面向对象操作
    @Autowired
    @Qualifier("restHighLevelClient")
    private RestHighLevelClient client;

    // 测试索引的创建 request
    @Test
    void testCreateIndex() throws IOException {
        // 创建索引请求
        CreateIndexRequest request = new CreateIndexRequest("ntuzy_index");
        // 执行请求  请求后获得响应
        CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);

        System.out.println(createIndexResponse);
    }

    // 测试获取索引
    @Test
    void testExistIndex() throws IOException {
        GetIndexRequest request = new GetIndexRequest("ntuzy_index");
        boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
        System.out.println(exists);
    }

    // 测试删除索引
    @Test
    void testDeleteIndex() throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest("ntuzy_index");
        AcknowledgedResponse delete = client.indices().delete(request, RequestOptions.DEFAULT);
        System.out.println(delete);
    }


    // 创建文档
    @Test
    void testAddDocument() throws IOException {
        // 创建对象
        User user = new User("ntuzy", 12);
        IndexRequest request = new IndexRequest("ntuzy_index");


        // 规则 put /ntuzy_index/_doc/1
        request.id("1");
        request.timeout(TimeValue.timeValueSeconds(1));

        request.timeout("1s");

        // 将我们的数据放入请求
        IndexRequest source = request.source(JSON.toJSONString(user), XContentType.JSON);

        // 客户端发送请求
        IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);

        System.out.println(indexResponse.toString());

        System.out.println(indexResponse.status());

    }

    // 获取文档  get /ntuzy_index/doc/1
    @Test
    void testIsExist() throws IOException {
        GetRequest request = new GetRequest("ntuzy_index", "1");

        // 不获取返回的 _source上下文
        request.fetchSourceContext(new FetchSourceContext(false));
        request.storedFields("_none_");

        boolean exists = client.exists(request, RequestOptions.DEFAULT);

        System.out.println(exists);

    }

    // 获取文档记录
    @Test
    void testGetDocument() throws IOException {
        GetRequest request = new GetRequest("ntuzy_index", "1");
        GetResponse response = client.get(request, RequestOptions.DEFAULT);

        System.out.println(response.getSourceAsString());
    }

    // 更新文档记录
    @Test
    void testUpdateDocument() throws IOException {
        UpdateRequest updateRequest = new UpdateRequest("ntuzy_index", "1");
        updateRequest.timeout("1s");
        User user = new User("ntuzy123", 22);
        updateRequest.doc(JSON.toJSONString(user), XContentType.JSON);
        UpdateResponse update = client.update(updateRequest, RequestOptions.DEFAULT);

        System.out.println(update.status());

    }

    // 删除文档
    @Test
    void testDeleteDocument() throws IOException {
        DeleteRequest deleteRequest = new DeleteRequest("ntuzy_index", "1");
        deleteRequest.timeout("1s");
        DeleteResponse deleteResponse = client.delete(deleteRequest, RequestOptions.DEFAULT);
        System.out.println(deleteResponse.status());
    }

    // 批量插入文档
    @Test
    void testBulkRequest() throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout("10s");

        ArrayList<User> userArr = new ArrayList<>();
        userArr.add(new User("ntuzy1",3));
        userArr.add(new User("ntuzy2",3));
        userArr.add(new User("ntuzy3",3));
        userArr.add(new User("ntuzy4",3));
        userArr.add(new User("ntuzy5",3));
        userArr.add(new User("ntuzy6",3));

        for (int i = 0; i < userArr.size(); i++) {
            bulkRequest.add(
                    new IndexRequest("ntuzy_index")
                            .id("" + (i + 1))
                            .source(JSON.toJSONString(userArr.get(i)), XContentType.JSON));
        }

        BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);

        System.out.println(bulkResponse.hasFailures());  // 是否失败 返回false代表成功


    }


    // 查询文档
    // SearchRequest    搜索请求
    // SearchSourceBuilder  条件构造
    // HighlightBuilder 构建高亮
    // TermAllQueryBuilder
    @Test
    void testSearch() throws IOException {
        SearchRequest request = new SearchRequest("ntuzy_index");

        // 构建搜索条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // QueryBuilders.termQuery 精确匹配
        // QueryBuilders.matchAllQuery
        QueryBuilder termQueryBuilder = QueryBuilders.termQuery("name","ntuzy1");
        sourceBuilder.query(termQueryBuilder);
//        sourceBuilder.from();
//        sourceBuilder.size();
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        //
        request.source(sourceBuilder);

        SearchResponse searchResponse = client.search(request, RequestOptions.DEFAULT);

//        System.out.println(searchResponse.getHits());

        System.out.println(JSON.toJSONString(searchResponse.getHits()));
        System.out.println("======================================================");

//        SearchHit[] hits = searchResponse.getHits().getHits();
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            System.out.println(hit.getSourceAsMap());
        }

    }

    @Test
    void contextLoads() {

    }

}
