package me.chr.hex;

import org.junit.jupiter.api.Test;
import org.neo4j.driver.*;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class HexApplicationTests {
    private static final String URI = "bolt://192.168.1.198:7687";
    private static final String USERNAME = "neo4j";
    private static final String PASSWORD = "yk12345678"; // ← 改成你设置的密码！

    @Test
    void contextLoads() {
        // 1. 创建驱动
        try (Driver driver = GraphDatabase.driver(URI, AuthTokens.basic(USERNAME, PASSWORD))) {

            // 2. 测试连接：执行简单查询
            try (Session session = driver.session()) {
                String message = session.executeWrite(tx -> {
                    // 创建一个测试节点
                    tx.run("MERGE (n:Test {name: 'Connected from Java!'}) RETURN n");
                    return "✅ 成功写入测试节点到 Neo4j！";
                });
                System.out.println(message);
            }

            // 3. 读取数据验证
            try (Session session = driver.session()) {
                Result result = session.run("MATCH (t:Test) RETURN t.name AS name LIMIT 1");
                if (result.hasNext()) {
                    String name = result.single().get("name").asString();
                    System.out.println("🔍 从 Neo4j 读取到节点: " + name);
                } else {
                    System.out.println("⚠️ 未找到 Test 节点");
                }
            }

        } catch (Exception e) {
            System.err.println("❌ 连接 Neo4j 失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
