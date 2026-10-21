CREATE TABLE IF NOT EXISTS ordens (
 id UUID PRIMARY KEY, conta_id TEXT NOT NULL, ativo TEXT NOT NULL,
 quantidade INTEGER NOT NULL CHECK (quantidade > 0), criada_em TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE IF NOT EXISTS outbox (
 id UUID PRIMARY KEY, aggregate_id UUID NOT NULL REFERENCES ordens(id),
 payload TEXT NOT NULL, criada_em TIMESTAMPTZ NOT NULL DEFAULT now(), publicada_em TIMESTAMPTZ
);
CREATE INDEX IF NOT EXISTS outbox_pendente ON outbox(criada_em) WHERE publicada_em IS NULL;
CREATE TABLE IF NOT EXISTS inbox (
 consumer_name TEXT NOT NULL, event_source TEXT NOT NULL, event_id UUID NOT NULL,
 processado_em TIMESTAMPTZ NOT NULL DEFAULT now(), PRIMARY KEY (consumer_name, event_source, event_id)
);
CREATE TABLE IF NOT EXISTS posicao (
 conta_id TEXT NOT NULL, ativo TEXT NOT NULL, quantidade BIGINT NOT NULL,
 PRIMARY KEY (conta_id, ativo)
);
