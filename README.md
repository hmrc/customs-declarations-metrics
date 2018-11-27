
# customs-declarations-metrics

This is a placeholder README.md for a new repository

### License

### Stuff left TODO
* consider simplifying some of the serialisation in models
* check indexType
* need test for INDEX
* This readME


This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").

    curl -X POST \
    http://localhost:9000/log-times \
    -H 'Accept: application/vnd.hmrc.1.0+json' \
    -d '{ "eventType": "DECLARATION", "conversationId": "dff783d7-44ee-4836-93d0-3242da7c225f", "eventStart": "2014-10-21T00:36:14.123Z", "eventEnd": "2014-10-21T00:38:14.123Z"}'