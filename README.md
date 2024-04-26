# Ring-SSL [![Build Status](https://github.com/ring-clojure/ring-ssl/actions/workflows/test.yml/badge.svg)](https://github.com/ring-clojure/ring-ssl/actions/workflows/test.yml)

Ring middleware for managing HTTPS requests.

This library includes middleware to parse the `X-Forwarded-Proto`
header, middleware that redirects HTTP requests to HTTPS URLs, and
middleware that adds the [Strict-Transport-Security][1] header to
responses.

[1]: https://en.wikipedia.org/wiki/HTTP_Strict_Transport_Security

## Installation

Add the following dependency to your `project.clj`:

    [ring/ring-ssl "0.3.0"]

## Documentation

* [API Docs](http://ring-clojure.github.io/ring-ssl/ring.middleware.ssl.html)

## Thanks

This project was originally conceived and developed by [James
Conroy-Finn][2] at [Listora][3].

[2]: http://jamesconroyfinn.com/
[3]: http://www.listora.com/

## License

Copyright © 2017 James Conroy-Finn, James Reeves

Released under the MIT license, same as Ring.
