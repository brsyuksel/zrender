# zrender

[![Build Status](https://travis-ci.com/brsyuksel/zrender.svg?branch=master)](https://travis-ci.com/brsyuksel/zrender)

it is a server written in scala that helps you to render static html from your javascript baked web application to deal with SEO problems you face when search engines or social networks visit your application.

it communicates with Chrome *(headless)* via [DevTools Protocol](https://chromedevtools.github.io/devtools-protocol/) to render. After getting rendered html from Chrome, it clears all javascript tags in content then returns.

## usage

just pull the docker image, run container with mapping the default server port *(9092)* and then visit localhost with url you want to render.

```shell
docker pull brsyuksel/zrender:latest
docker run --rm -p 9092:9092 brsyuksel/zrender:latest
curl http://localhost:9092/https://www.google.com
```

## motivation

after trying [prerender](https://github.com/prerender/prerender) at work, I decided to implement a prerender server in scala to learn functional programming technics and make it as an example for who wants to learn fp.

I'm aware that the source code has some problems on using libraries and testing, but I will have been noting down changes related fp-things to make everyone able to see evolution of code with me.

It may help you to learn [scalaz](https://scalaz.github.io/7/), [ZIO](https://zio.dev/), [fs2](https://fs2.io/), [fs2-http](https://github.com/Spinoco/fs2-http), [circe](https://circe.github.io/circe/), [pureconfig](https://pureconfig.github.io/) and also [scalacheck](https://www.scalacheck.org/).

## notes

Special thanks:
	
+ [Adam Chlupacek](https://github.com/AdamChlupacek)