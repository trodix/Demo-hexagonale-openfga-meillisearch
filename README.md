## Setup

```bash
$ fga store create --name demo                                                                                                                                 ✘ 1   25.0.1  15:12:22 
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
$ fga model transform --file src/main/resources/openfga/demo.fga > /tmp/openfga-model.json && fga model write --store-id 01KFNK6K4HZ3HHBR61ZTCGVM2E --file /tmp/openfga-model.json
{
  "authorization_model_id":"01KFNMMKG2R81B642WWC5S3368"
}
```