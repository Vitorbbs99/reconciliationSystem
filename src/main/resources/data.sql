-- Limpa registros existentes
TRUNCATE TABLE faturas RESTART IDENTITY;

-- Cria TXIDs sequenciais de 'TXID-1' até 'TXID-200' com valor esperado fixo de R$ 100,00
INSERT INTO faturas (txid, valor_esperado, status, data_vencimento, data_criacao)
SELECT
    'TXID-' || seq AS txid,
    100.00 AS valor_esperado,
    'PENDENTE' AS status,
    CURRENT_TIMESTAMP + INTERVAL '1 day' AS data_vencimento,
    CURRENT_TIMESTAMP AS data_criacao
FROM generate_series(1, 3000) AS seq;

INSERT INTO faturas (txid, valor_esperado, status, data_vencimento, data_criacao)
VALUES
  ('TXID-3005', 100.00, 'PENDENTE',NOW() + INTERVAL '1 day', NOW())
