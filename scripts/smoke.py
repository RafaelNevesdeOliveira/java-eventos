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
parser.add_argument('--aula',choices=('1',),default='1')
lesson=parser.parse_args().aula
account='smoke-'+str(uuid.uuid4())
body={'contaId':account,'ativo':'PETR4','quantidade':10}
if lesson=='1':
 wait_for(lambda:request(A1,'/ordens',body,expected=202),'aula 1 recebendo comando')
 request(A1,'/ordens',dict(body,quantidade=0),expected=400)
 print('OK aula 1: comando confirmado pelo RabbitMQ e entrada inválida rejeitada; fan-out: conferir logs')
 raise SystemExit(0)
