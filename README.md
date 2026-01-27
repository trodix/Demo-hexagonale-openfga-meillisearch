## Setup

```bash
fga store create --name demo
````
```json                                                                                                                                ✘ 1   25.0.1  15:12:22 
{
  "store": {
    "created_at":"2026-01-23T14:12:46.353761Z",
    "id":"01KFNK6K4HZ3HHBR61ZTCGVM2E",
    "name":"demo",
    "updated_at":"2026-01-23T14:12:46.353761Z"
  }
}
```

```bash
fga model transform --file src/main/resources/openfga/demo.fga > /tmp/openfga-model.json && fga model write --store-id 01KFNK6K4HZ3HHBR61ZTCGVM2E --file /tmp/openfga-model.json
````
```json
{
  "authorization_model_id":"01KFNMMKG2R81B642WWC5S3368"
}
```

```bash
fga --store-id 01KFZCM007KQ390VE428JS4CPT tuple write user:admin admin tenant:tenant1
```

```bash
fga --store-id 01KFZCM007KQ390VE428JS4CPT tuple write tenant:tenant1 parent entity:tenant1/product
```
```json
{
  "successful": [
    {
      "object":"entity:tenant1/product",
      "relation":"parent",
      "user":"tenant:tenant1"
    }
  ]
}
```

## Script to setup empty OpenFga server

```bash
STORE_ID=$(fga store create --name demo | jq -r '.store.id')
AUTH_MODEL_ID=$(fga model transform --file src/main/resources/openfga/demo.fga > /tmp/openfga-model.json && fga model write --store-id $STORE_ID --file /tmp/openfga-model.json | jq -r '.authorization_model_id')
fga --store-id $STORE_ID tuple write user:admin admin tenant:tenant1
fga --store-id $STORE_ID tuple write tenant:tenant1 parent entity:tenant1/product
sed -i "s/\(store-id:\s\+\)[A-Z0-9]\{26\}\(.*\)/\1$STORE_ID\2/" src/main/resources/application-dev.yaml
```