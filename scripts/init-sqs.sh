#!/bin/sh

echo "==== 1. Criando infraestrutura SQS ===="
aws --endpoint-url=http://localhost:4566 --region sa-east-1 sqs create-queue --queue-name pix-transactions-dlq

DLQ_ARN=$(aws --endpoint-url=http://localhost:4566 --region sa-east-1 sqs get-queue-attributes --queue-url http://localhost:4566/000000000000/pix-transactions-dlq --attribute-names QueueArn --query "Attributes.QueueArn" --output text)

aws --endpoint-url=http://localhost:4566 --region sa-east-1 sqs create-queue \
  --queue-name pix-transactions-queue \
  --attributes "{\"RedrivePolicy\": \"{\\\"deadLetterTargetArn\\\":\\\"$DLQ_ARN\\\",\\\"maxReceiveCount\\\":\\\"3\\\"}\", \"VisibilityTimeout\": \"30\"}"

echo "==== 2. Injetando 100 mensagens de teste em lotes (Batch) ===="
QUEUE_URL="http://localhost:4566/queue/sa-east-1/000000000000/pix-transactions-queue"

echo "=== 1. Enviando mensagem de teste especifica (TXID-3005) ==="

PAYLOAD_3005="{\"idTransacaoPix\":\"PIX-3005\",\"txid\":\"TXID-3005\",\"valorPago\":50.00,\"dataPagamento\":\"2026-08-02T11:48:00\"}"
ENTRY_3005="{\"Id\":\"msg_3005\",\"MessageBody\":\"$(echo $PAYLOAD_3005 | sed 's/"/\\"/g')\"}"

aws --endpoint-url=http://localhost:4566 --region sa-east-1 sqs send-message-batch \
  --queue-url "$QUEUE_URL" \
  --entries "[$ENTRY_3005]" > /dev/null

echo "✅ Mensagem TXID-3005 enviada com sucesso!"
echo ""

echo "=== 2. Iniciando ingestao em massa (3000 mensagens) ==="

# Envia 300 lotes de 10 mensagens (total = 3000 mensagens)
for batch in $(seq 0 299); do
  ENTRIES="["
  for j in $(seq 1 10); do
    i=$((batch * 10 + j))
    PAYLOAD="{\"idTransacaoPix\":\"PIX-REG-$i\",\"txid\":\"TXID-$i\",\"valorPago\":100.00,\"dataPagamento\":\"2026-07-30T10:00:00\"}"

    # Monta o objeto de cada entrada do lote
    ENTRY="{\"Id\":\"msg_$i\",\"MessageBody\":\"$(echo $PAYLOAD | sed 's/"/\\"/g')\"}"

    if [ $j -gt 1 ]; then
      ENTRIES="$ENTRIES,$ENTRY"
    else
      ENTRIES="$ENTRIES$ENTRY"
    fi
  done
  ENTRIES="$ENTRIES]"

  # Dispara a requisição em lote (10 de uma vez)
  aws --endpoint-url=http://localhost:4566 --region sa-east-1 sqs send-message-batch \
    --queue-url "$QUEUE_URL" \
    --entries "$ENTRIES" > /dev/null

  echo "Lote $((batch + 1))/300 enviado (Mensagens $((batch * 10 + 1)) ate $((batch * 10 + 10)))"
done

echo "==== Ingestao em lote concluida com sucesso! (3000 mensagens) ===="
