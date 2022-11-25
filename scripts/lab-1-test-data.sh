#!/bin/bash
curl -X POST $API -H "Content-type: Application/JSON" -d '{"name":"widget", "price":15}'
curl -X POST $API -H "Content-type: Application/JSON" -d '{"name":"bolt", "price":5}'
curl -X POST $API -H "Content-type: Application/JSON" -d '{"name":"wrench", "price":12}'
curl -X POST $API -H "Content-type: Application/JSON" -d '{"name":"driver", "price":4}'
curl -X POST $API -H "Content-type: Application/JSON" -d '{"name":"socket", "price":3}'
curl -X POST $API -H "Content-type: Application/JSON" -d '{"name":"hammer", "price":15}'
curl -X POST $API -H "Content-type: Application/JSON" -d '{"name":"hammer", "price":15}'
curl -X POST $API -H "Content-type: Application/JSON" -d '{"name":"hammer", "price":15}'