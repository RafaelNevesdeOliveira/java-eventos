#!/usr/bin/env python3
"""Verificação real via HTTP; executar após iniciar a aplicação da aula escolhida."""
import argparse, json, time, uuid
from http.client import HTTPException
from urllib.request import Request, urlopen
from urllib.error import HTTPError, URLError
A1='http://localhost:8080/api'
A2='http://localhost:8082/api'
def request(base,path,body=None,method=None,expected=200):
 data=None if body is None else json.dumps(body).encode()
 req=Request(base+path,data=data,headers={'Content-Type':'application/json'},method=method or ('POST' if data is not None else 'GET'))
 try:
  with urlopen(req,timeout=20) as res: code=res.status; raw=res.read()
 except HTTPError as e: code=e.code;raw=e.read()
 assert code==expected, (path,code,raw.decode())
 return json.loads(raw) if raw else None

def wait_for(fn,description,seconds=90):
 end=time.monotonic()+seconds;last=None
 while time.monotonic()<end:
  try:
   last=fn()
   if last:return last
  except (OSError,HTTPException,AssertionError):pass
  time.sleep(1)
 raise AssertionError(f'Tempo excedido: {description}; último resultado: {last}')

parser=argparse.ArgumentParser(description="Verifica uma aula sem depender da outra aplicação")
parser.add_argument('--aula',choices=('1','2'),default='2')
lesson=parser.parse_args().aula
account='smoke-'+str(uuid.uuid4())
body={'contaId':account,'ativo':'PETR4','quantidade':10}
if lesson=='1':
 wait_for(lambda:request(A1,'/ordens',body,expected=202),'aula 1 recebendo comando')
 request(A1,'/ordens',dict(body,quantidade=0),expected=400)
 print('OK aula 1: comando confirmado pelo RabbitMQ e entrada inválida rejeitada; fan-out: conferir logs')
 raise SystemExit(0)
wait_for(lambda:request(A2,'/lab/status'),'aula 2 disponível')
request(A2,'/ordens',dict(body,quantidade=0),expected=400)
print('OK validação de entrada')
# Sem tráfego concorrente durante o teste de rollback.
before=request(A2,'/lab/status')
request(A2,'/ordens?falhar=true',body,expected=503)
after=request(A2,'/lab/status')
assert (before['ordens'],before['outbox'])==(after['ordens'],after['outbox'])
print('OK rollback de ordem + outbox')
try:
 request(A2,'/lab/relay/pausar',method='POST')
 event=request(A2,'/ordens',body,expected=201)
 time.sleep(3)
 assert not [p for p in request(A2,'/posicoes') if p['conta_id']==account]
 assert any(x['id']==event['id'] and x['publicada_em'] is None for x in request(A2,'/outbox'))
 print('OK relay pausado: evento pendente e projeção ausente')
finally:
 request(A2,'/lab/relay/retomar',method='POST')
def position():
 return next((p for p in request(A2,'/posicoes') if p['conta_id']==account),None)
wait_for(position,'projeção atualizada')
assert position()['quantidade']==10
wait_for(lambda:any(x['id']==event['id'] and x['publicada_em'] is not None for x in request(A2,'/outbox')),'primeira publicação confirmada')
print('OK consistência eventual: projeção convergiu')
for _ in range(3):
 request(A2,f"/lab/outbox/{event['id']}/reenviar",method='POST')
 wait_for(lambda:any(x['id']==event['id'] and x['publicada_em'] is not None for x in request(A2,'/outbox')),'reenvio confirmado')
 time.sleep(2)
 assert position()['quantidade']==10
print('OK três reenvios: posição continua 10')
print('Todos os cenários HTTP passaram. Conta:',account)
