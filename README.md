
# Customs Declarations Metrics

The Customs Declarations Metrics service is used by customs-declarations and customs-notification to handle sending metrics to graphite. These metric events are persisted in a mongo collection associated with this service.


## Development Setup
- Run locally: `sbt run` which runs on port `9827` by default

##  Service Manager Profiles
The Customs Declarations Metrics service can be run locally from Service Manager, using the following profiles:

| Profile Details                       | Command                                                           | Description                                                    |
|---------------------------------------|:------------------------------------------------------------------|----------------------------------------------------------------|
| CUSTOMS_DECLARATION_ALL               | sm2 --start CUSTOMS_DECLARATION_ALL                               | To run all CDS applications.                                   |
| CUSTOMS_INVENTORY_LINKING_EXPORTS_ALL | sm2 --start CUSTOMS_INVENTORY_LINKING_EXPORTS_ALL                 | To run all CDS Inventory Linking Exports related applications. |
| CUSTOMS_INVENTORY_LINKING_IMPORTS_ALL | sm2 --start CUSTOMS_INVENTORY_LINKING_IMPORTS_ALL                 | To run all CDS Inventory Linking Imports related applications. |


## Run Tests
- Run Unit Tests: `sbt test`
- Run Integration Tests: `sbt IntegrationTest/test`
- Run Unit and Integration Tests: `sbt test IntegrationTest/test`
- Run Unit and Integration Tests with coverage report: `./run_all_tests.sh`<br/> which runs `sbt clean scalastyle coverage test it:test coverageReport"`

### Acceptance Tests
To run the CDS acceptance tests, see [here](https://github.com/hmrc/customs-automation-test).

### Performance Tests
To run performance tests, see [here](https://github.com/hmrc/customs-declaration-performance-test).

## Customs Declarations Metrics specific routes

| Path         | Supported Methods | Description                         |
|--------------|:-----------------:|-------------------------------------|
| `/log-times` |       POST        | Allows submission of Timed Metrics. |


### curl command
```
curl -v -X POST http://localhost:9000/log-times \
    -H 'Accept: application/vnd.hmrc.1.0+json' \
    -d '{ "eventType": "DECLARATION", "conversationId": "dff783d7-44ee-4836-93d0-3242da7c225f", "eventStart": "2014-10-21T00:36:14.123Z", "eventEnd": "2014-10-21T00:38:14.123Z"}'
```

```
curl -v -X POST http://localhost:9000/log-times \
    -H 'Accept: application/vnd.hmrc.1.0+json' \
    -d '{ "eventType": "NOTIFICATION", "conversationId": "dff783d7-44ee-4836-93d0-3242da7c225f", "eventStart": "2014-10-21T00:36:14.123Z", "eventEnd": "2014-10-21T00:38:14.123Z"}'
```

A call with a **DECLARATION** event type will trigger a call to graphite for a metric named **'declaration-digital'**

A call with a **NOTIFICATION** event type will trigger 3 calls to graphite one for a metric named **'declaration-round-trip'**, another for a metric named **'notification-digital'** and finally a metric named **'declaration-digital-total'**


### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").

   