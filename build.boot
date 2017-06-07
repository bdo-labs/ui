(def +title+ "ui")
(def +description+ "A Straight-Forward Library for Composing User-Interfaces")
(def +url+ "https://github.com/bdo-labs/ui")


(set-env!
 :source-paths #{"src/cljc" "src/cljs"}
 :resource-paths #{"resources"}
 :dependencies '[;; Project Dependencies
                 [clj-time "0.13.0"]
                 [cljsjs/hammer "2.0.4-5"]
                 [com.andrewmcveigh/cljs-time "0.5.0"]
                 [day8.re-frame/undo "0.3.2"]
                 [day8/re-frame-tracer "0.1.1-SNAPSHOT"]
                 [garden "1.3.2"]
                 [markdown-clj "0.9.99"]
                 [ns-tracker "0.3.1" :scope "test"]
                 [org.clojars.stumitchell/clairvoyant "0.2.1"]
                 [org.clojure/clojure "1.9.0-alpha17" :scope "provided"]
                 [org.clojure/spec.alpha "0.1.123"]
                 [org.clojure/clojurescript "1.9.562"]
                 [org.clojure/core.async "0.3.443"]
                 [org.clojure/test.check "0.9.0" :exclude [org.clojure/clojure] :scope "test"]
                 [re-frame "0.9.4"]
                 [re-frisk "0.4.5" :scope "test"]
                 [reagent "0.6.2"]
                 [secretary "1.2.3"]
                 [tongue "0.2.2"]

                 ;; Build Dependencies
                 [boot/core "2.7.1" :scope "provided"]
                 [binaryage/devtools "0.9.4" :scope "test"]
                 [binaryage/dirac "1.2.9" :scope "test"]
                 [adzerk/boot-cljs "2.0.0"  :exclude [org.clojure/clojurescript] :scope "test"]
                 [adzerk/boot-cljs-repl "0.3.3" :scope "test"]
                 [afrey/boot-asset-fingerprint "1.3.1" :scope "test"]
                 [adzerk/boot-reload "0.5.1" :scope "test"]
                 [adzerk/boot-test "1.2.0" :scope "test"]
                 [crisptrutski/boot-cljs-test "0.3.1" :scope "test"]
                 [danielsz/boot-autoprefixer "0.1.0" :scope "test"]
                 [degree9/boot-npm "1.4.0" :scope "test"]
                 [degree9/boot-semver "1.6.0" :scope "test"]
                 [degree9/boot-semgit "1.2.0" :scope "test"]
                 [funcool/boot-codeina "0.1.0-SNAPSHOT" :scope "test"]
                 [hendrick/boot-medusa "0.1.1" :scope "test"]
                 [org.martinklepsch/boot-garden "1.3.2-0" :scope "test"]
                 ;; [powerlaces/boot-figreload "0.1.1-SNAPSHOT" :scope "test"]
                 [powerlaces/boot-cljs-devtools "0.2.0" :scope "test"]
                 [com.cemerick/piggieback "0.2.1"  :scope "test"]
                 [org.clojure/tools.nrepl "0.2.12" :scope "test"]
                 [tolitius/boot-check "0.1.4" :scope "test"]
                 [pandeiro/boot-http "0.8.3" :scope "test"]
                 [weasel "0.7.0"  :scope "test"]])


(require '[adzerk.boot-cljs :refer [cljs]]
         '[adzerk.boot-cljs-repl :refer [cljs-repl]]
         '[adzerk.boot-reload :refer [reload]]
         '[adzerk.boot-test :refer [test]]
         '[crisptrutski.boot-cljs-test :refer [test-cljs report-errors!]]
         '[danielsz.autoprefixer :refer [autoprefixer]]
         '[degree9.boot-npm :refer [npm]]
         '[degree9.boot-semver :refer :all]
         '[degree9.boot-semgit :refer :all]
         '[funcool.boot-codeina :refer :all]
         '[hendrick.boot-medusa :refer :all]
         '[org.martinklepsch.boot-garden :refer [garden]]
         ;; '[powerlaces.boot-figreload :refer [reload]]
         '[powerlaces.boot-cljs-devtools :refer [cljs-devtools dirac]]
         '[tolitius.boot-check :as check]
         '[pandeiro.boot-http :refer [serve]]
         '[afrey.boot-asset-fingerprint :refer [asset-fingerprint]])


(task-options!
 apidoc {:title          +title+
         :version        "latest"
         :description    +description+
         :src-uri        (str +url+ "/tree/master/")
         :src-uri-prefix "#L"
         :sources        (get-env :source-paths)}
 pom {:project     +title+
      :description +description+
      :url         +url+
      :license     {"The MIT License (MIT)"
                    "http://opensource.org/licenses/mit-license.php"}}
 repl {:middleware '[cemerick.piggieback/wrap-cljs-repl]}
 target {:dir #{"target"}}
 test-cljs {:js-env :phantom})


(deftask pre-requisits
  "Install pre-requisits"
  []
  (comp
   (npm :install {:postcss-cli "latest"
                  :autoprefixer "latest"}
        :cache-key ::cache)
   (target)))


(deftask readme
  "Generate a Clojure-common file from the README to be consumed
  client-side"
  []
  (let [content (str "(ns ui.readme)\n\n(def content \"" (slurp "README.md") "\")")
        out "src/cljc/ui/readme.cljc"]
    (doto out
      (spit content))
    identity))



(deftask styles
  "Compile garden-styles and add browser-prefixes. Note that this
  requires that `postcss-cli` and `autoprefixer` is installed"
  []
  (comp (garden :output-to "css/ui.css"
                :styles-var 'ui.styles/screen)
        (garden :output-to "css/docs.css"
                :styles-var 'ui.styles/docs)
        (autoprefixer :exec-path "target/node_modules/postcss-cli/bin/postcss"
                      :files ["ui.css" "docs.css"] :browsers ">= 50%")))


(deftask dev
  "Interactive development-build"
  [s speak? bool "Notify when the build is completed"]
  (comp (git-pull :branch "origin" "master")
        (pre-requisits)
        (readme)
        (serve)
        (watch)
        (if speak? (speak) identity)
        (reload :on-jsload 'ui.core/mount-root)
        (styles)
        (cljs-repl)
        (cljs-devtools)
        (cljs :ids #{"ui"}
              :optimizations :none
              :source-map true
              :compiler-options {:parallel-build true})
        (asset-fingerprint :extensions [".css" ".html"] :skip true)))


(deftask prod
  "Static production-build"
  [s speak? bool "Notify when the build is completed"]
  (comp (pre-requisits)
        (readme)
        (if speak? (speak) identity)
        (styles)
        (cljs :ids #{"ui"}
              :optimizations :advanced
              :compiler-options {:closure-defines    {"goog.DEBUG" false}
                                 :pretty-print       false
                                 :pseudo-names       true
                                 :static-fns         true
                                 :parallel-build     true
                                 :optimize-constants true})
        (asset-fingerprint :extensions [".css" ".html"])
        (target)))


(deftask test-once
  "Run tests once. Typically used by the CI-runner"
  [s speak? bool "Notify when the build is completed"]
  (merge-env! :source-paths #{"test"})
  (comp (if speak? (speak) identity)
        (test-cljs)))


(deftask test-auto
  "Run tests continuously"
  [s speak? bool "Notify when the build is completed"]
  (merge-env! :source-paths #{"test"})
  (comp (watch)
        (if speak? (speak) identity)
        (test-cljs)))


(deftask check-src []
  "Linting & other code-conformance tests"
  (merge-env! :source-paths #{"test"})
  (comp
    (check/with-yagni)
    (check/with-eastwood)
    (check/with-kibit)
    (check/with-bikeshed)))


(deftask deploy
  "Bump version and push to Github. Accepted pull-requests are
  automatically published to Clojars"
  [s speak?        bool "Notify when deployment is completed"
   b bump   VALUE  kw   "What to bump (major minor or patch)"]
  (comp (version bump 'inc)
        (git-push)))
