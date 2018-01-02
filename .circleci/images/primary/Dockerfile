FROM circleci/clojure:boot-2.7.1-browsers

ENV LEIN_INSTALL=/usr/local/bin/
ENV BOOT_JVM_OPTIONS="-Xms512m -Xmx1024m -Djava.awt.headless=true"
ENV BOOT_WATCHERS_DISABLE="yes"

RUN mkdir -p $LEIN_INSTALL \
  && wget -q https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein \
  && chmod +x lein \
  && sudo mv lein $LEIN_INSTALL

ENV PATH=$PATH:$LEIN_INSTALL
ENV LEIN_ROOT 1

RUN git clone https://github.com/noprompt/garden \
 && cd garden \
 && git checkout v2.0.0 \
 && lein install
