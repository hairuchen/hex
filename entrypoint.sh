#!/bin/bash

echo "🚀 正在初始化 OpenTelemetry 探针..."

# 👉 定义探针路径变量（这里要和 docker-compose.yml 里的挂载路径一致）
AGENT_PATH="/opt/otel/opentelemetry-javaagent.jar"

# 检查探针文件是否存在
if [ -f "$AGENT_PATH" ]; then
  echo "✅ 探针已加载: $AGENT_PATH"
  # 👉 这里使用变量
  JAVA_AGENT_OPTS="-javaagent:$AGENT_PATH"
else
  echo "⚠️ 未找到探针文件: $AGENT_PATH，将以无监控模式运行"
  JAVA_AGENT_OPTS=""
fi

# 设置默认值
OTEL_ENDPOINT=${OTEL_EXPORTER_OTLP_ENDPOINT:-"http://otel-collector:4317"}
SERVICE_NAME=${OTEL_SERVICE_NAME:-"hex-backend-default"}

echo "   - 服务名称: $SERVICE_NAME"
echo "   - 发送地址: $OTEL_ENDPOINT"

# 启动应用
# 👉 这里加上 $JAVA_AGENT_OPTS
java \
  $JAVA_AGENT_OPTS \
  -Dotel.service.name=$SERVICE_NAME \
  -Dotel.exporter.otlp.endpoint=$OTEL_ENDPOINT \
  -Dotel.exporter.otlp.protocol=grpc \
  -jar /app/app.jar