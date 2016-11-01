FROM up-registry.ft.com/coco/dropwizardbase

ADD . /

RUN apk --update add git \
 && HASH=$(git log -1 --pretty=format:%H) \
 && BUILD_NUMBER=$(cat ../buildnum.txt) \
 && BUILD_URL=$(cat ../buildurl.txt) \
 && mvn install -Dbuild.git.revision=$HASH -Dbuild.git.revision=$HASH -Dbuild.number=$BUILD_NUMBER -Dbuild.url=$BUILD_URL -Djava.net.preferIPv4Stack=true \
 && rm -f target/methode-image-model-mapper-*sources.jar \
 && mv target/methode-image-set-mapper-*.jar /methode-image-set-mapper.jar \
 && mv methode-image-set-mapper.yaml /config.yaml \
 && apk del git \
 && rm -rf /var/cache/apk/* \
 && rm -rf /root/.m2/*

EXPOSE 8080 8081

CMD java $JAVA_OPTS \
     -Ddw.server.applicationConnectors[0].port=8080 \
     -Ddw.server.adminConnectors[0].port=8081 \
     -Ddw.binaryTransformer.hostAddress=$VULCAN_HOST:8080 \
     -Ddw.externalBinaryUrlBasePath=$S3_URL \
     -Ddw.logging.appenders[0].logFormat="%-5p [%d{ISO8601, GMT}] %c: %X{transaction_id} %replace(%m%n[%thread]%xEx){'\n', '|'}%nopex%n" \
     -jar methode-image-set-mapper.jar server config.yaml
