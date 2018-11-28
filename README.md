
# customs-declarations-metrics

This service is used by customs-declarations and customs-notification to handle sending metrics to graphite.
The metric events are persisted in Mongo
 
### Stuff left TODO
* improve coverage for exception cases

| Path                                                                                                                            |  Method  | Description                                |
|---------------------------------------------------------------------------------------------------------------------------------|----------|--------------------------------------------|
| [`/log-times`](#user-content-post-log-times)                                                                           |   `POST` | Allows submission of Timed Metrics |


### POST Log Times 
#### `POST /log-times`
Allows submission of Timed Metrics

##### curl command
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

   