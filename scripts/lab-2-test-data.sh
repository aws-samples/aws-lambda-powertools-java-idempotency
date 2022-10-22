#!/bin/bash
KEY=PK

aws dynamodb scan --table-name $TABLE --attributes-to-get "$KEY" \
  --query "Items[].$KEY.S" --output text | \
  tr "\t" "\n" | \
  xargs -t -I keyvalue aws dynamodb delete-item --table-name $TABLE \
  --key "{\"$KEY\": {\"S\": \"keyvalue\"}}"
  
curl -X POST $API -H "Content-type: Application/JSON" -d '{"name":"wrench", "price":4}'
curl -X POST $API -H "Content-type: Application/JSON" -d '{"name":"nail", "price":1}'
curl -X POST $API -H "Content-type: Application/JSON" -d '{"name":"saw", "price":50}'
curl -X POST $API -H "Content-type: Application/JSON" -d '{"name":"axe", "price":12}'
curl -X POST $API -H "Content-type: Application/JSON" -d '{"name":"screwdriver", "price":6}'
curl -X POST $API -H "Content-type: Application/JSON" -d '{"name":"allen wrench", "price":1}'
curl -X POST $API -H "Content-type: Application/JSON" -d '{"name":"bolt cutter", "price":35}'
curl -X POST $API -H "Content-type: Application/JSON" -d '{"name":"bolt cutter", "price":35}'
curl -X POST $API -H "Content-type: Application/JSON" -d '{"name":"bolt cutter", "price":35}'

curl -X GET $API/ForceError