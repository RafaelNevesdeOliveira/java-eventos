#!/usr/bin/env python3
"""Demonstra compatibilidade JSON Schema com um registry local real."""
import json,time,uuid
from http.client import HTTPException
from pathlib import Path
from urllib.request import Request,urlopen
from urllib.error import HTTPError,URLError
BASE='http://localhost:8085'
SUBJECT='ordem-aceita-data-'+str(uuid.uuid4())
ROOT=Path(__file__).resolve().parents[1]/'contratos'
def req(path,body=None,method=None,expected=200):
 data=None if body is None else json.dumps(body).encode()
 r=Request(BASE+path,data=data,headers={'Content-Type':'application/vnd.schemaregistry.v1+json'},method=method or ('POST' if data else 'GET'))
 try:
  with urlopen(r,timeout=10) as res: code=res.status; raw=res.read()
 except HTTPError as e:code=e.code;raw=e.read()
 assert code==expected,(code,raw.decode())
 return json.loads(raw)
def schema(name):return {'schemaType':'JSON','schema':(ROOT/name).read_text()}
for attempt in range(90):
 try:req('/subjects');break
 except (OSError,HTTPException,AssertionError):time.sleep(1)
else:raise SystemExit('Registry indisponível. Inicie o perfil contratos e consulte os logs.')
req('/config/'+SUBJECT,{'compatibility':'BACKWARD'},method='PUT')
req('/subjects/'+SUBJECT+'/versions',schema('ordem-aceita-v1.json'))
good=schema('ordem-aceita-v2-compativel.json')
result=req('/compatibility/subjects/'+SUBJECT+'/versions/latest',good)
assert result['is_compatible'],result
req('/subjects/'+SUBJECT+'/versions',good)
print('OK campo opcional aceito sob BACKWARD')
bad=schema('ordem-aceita-v3-incompativel.json')
result=req('/compatibility/subjects/'+SUBJECT+'/versions/latest',bad)
assert not result['is_compatible'],result
req('/subjects/'+SUBJECT+'/versions',bad,expected=409)
print('OK troca de tipo rejeitada com HTTP 409')
print('Subject:',SUBJECT)
