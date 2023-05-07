[![Build and test](https://github.com/probakowski/zielonykod/actions/workflows/build.yml/badge.svg)](https://github.com/probakowski/zielonykod/actions/workflows/build.yml)
[![CodeQL](https://github.com/probakowski/zielonykod/actions/workflows/codeql.yml/badge.svg)](https://github.com/probakowski/zielonykod/actions/workflows/codeql.yml)

## ING Zielony Kod

This repository contains the code for [Zielona Tesla za Zielony kod](https://www.ing.pl/pionteching) tournament.

### Running

Run `./build.sh` to build the project

Run `./run.sh` to run. The server is ready when you see following message in log:

`HTTP Server is now available at http://localhost:8080/`

There is also set of E2E tests that can be run with `./gradlew e2e`

### Static application security testing (SAST)

SAST is done using [spotbugs](https://spotbugs.github.io/) + [find-sec-bugs](https://find-sec-bugs.github.io/) during
build and CodeQL analysis in Github action after every commit. Check badges at the top of README.md

### Licenses

This code is distributed under MIT license.

Libraries used:

- [activej](https://github.com/activej/activej) - [Apache 2.0 license](https://github.com/activej/activej/blob/master/LICENSE)
- [dsl-json](https://github.com/ngs-doo/dsl-json) - [BSD-3-Clause license](https://github.com/ngs-doo/dsl-json/blob/master/LICENSE)
- [junit](https://github.com/junit-team/junit5/) - [Eclipse Public License](https://github.com/junit-team/junit5/blob/main/LICENSE.md)
- [log4j](https://github.com/apache/logging-log4j2) - [Apache 2.0 license](https://github.com/apache/logging-log4j2/blob/2.x/LICENSE.txt)
- [slf4j](https://github.com/qos-ch/slf4j) - [MIT license](https://github.com/qos-ch/slf4j/blob/master/LICENSE.txt)
- [jmh](https://github.com/openjdk/jmh) - [GPL-2.0 license](https://github.com/openjdk/jmh/blob/master/LICENSE)