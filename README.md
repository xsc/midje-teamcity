# midje-teamcity

__midje-teamcity__ is a [Midje](https://github.com/marick/Midje) emitter for [TeamCity](http://www.jetbrains.com/teamcity/). It aims to do for Midje what
[lein-teamcity](https://github.com/nd/lein-teamcity) does for `clojure.test`.

[![endorse](https://api.coderwall.com/xsc/endorsecount.png)](https://coderwall.com/xsc)

I will open a pull request at Midje itself once this has proven it's not destroying machines.

## Usage

__Leiningen__ ([via Clojars](https://clojars.org/midje-teamcity))

Add this to your `:dependencies` (or in a separate profile):

```clojure
[midje-teamcity "0.1.0"]
```

__Midje__

Add this to your Midje [configuration file](https://github.com/marick/Midje/wiki/Configuration-files):

```clojure
(change-defaults :emitter 'midje-teamcity.emitter)
```

__Run Tests__

You can now run your tests using, e.g. `lein-midje`:

```bash
$ lein midje
##teamcity[testSuiteStarted name='my.test']
##teamcity[testStarted name='about something to test.' captureStandardOutput='true']
...
```

## License

Copyright &copy; 2014 Yannick Scherer

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
