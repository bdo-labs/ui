(def +title+ 'ui)
(def +description+ "A Straight-Forward Library for Composing User-Interfaces")
(def +url+ (str "https://github.com/bdo-labs/" +title+))


(set-env!
 :source-paths #{"src/cljc" "src/cljs"}
 :resource-paths #{"resources" "src/cljc" "src/cljs"}
 :dependencies '[;; Project Dependencies
                 [com.andrewmcveigh/cljs-time "0.5.1"]
                 [org.clojure/clojure "1.9.0" :scope "provided"]
                 [org.clojure/clojurescript "1.9.946" :scope "provided"]
                 [org.clojure/core.async "0.3.443"]
                 [venantius/accountant "0.2.0"]
                 [com.cemerick/url "0.1.1"]
                 [clj-time "0.14.0"]
                 [garden "2.0.0-alpha1"]
                 [markdown-clj "1.0.1"]
                 [re-frame "0.10.3-alpha1"]
                 [reagent "0.8.0-alpha2"]
                 [secretary "1.2.3"]
                 [tongue "0.2.2"]

                 ;; Build Dependencies
                 [org.clojars.stumitchell/clairvoyant "0.2.1" :scope "test"]
                 [day8.re-frame/trace "0.1.13" :scope "test"]
                 [adzerk/boot-cljs "2.1.4" :scope "test"]
                 [adzerk/boot-cljs-repl "0.3.3" :scope "test"]
                 [adzerk/boot-reload "0.5.2" :scope "test"]
                 [adzerk/boot-test "1.2.0" :scope "test"]
                 [binaryage/devtools "0.9.7" :scope "test"]
                 [com.cemerick/piggieback "0.2.2"  :scope "test"]
                 [crisptrutski/boot-cljs-test "0.3.4" :scope "test"]
                 [danielsz/boot-autoprefixer "0.1.0" :scope "test"]
                 [degree9/boot-npm "1.4.0" :scope "test"]
                 [degree9/boot-semgit "1.2.1" :scope "test"]
                 [degree9/boot-semver "1.7.0" :scope "test"]
                 [funcool/boot-codeina "0.1.0-SNAPSHOT" :scope "test"]
                 [hendrick/boot-medusa "0.1.1" :scope "test"]
                 [org.clojure/test.check "0.10.0-alpha2" :scope "test"]
                 [org.martinklepsch/boot-garden "1.3.2-0" :scope "test"]
                 [pandeiro/boot-http "0.8.3" :scope "test"]
                 [afrey/ring-html5-handler "1.1.1" :scope "test"]
                 [powerlaces/boot-cljs-devtools "0.2.0" :scope "test"]
                 [ns-tracker "0.3.1" :scope "test"]
                 [weasel "0.7.0"  :scope "test"]])


(require '[adzerk.boot-cljs :refer [cljs]]
         '[adzerk.boot-cljs-repl :refer [cljs-repl]]
         '[adzerk.boot-reload :refer [reload]]
         '[adzerk.boot-test :refer [test]]
         '[crisptrutski.boot-cljs-test :refer [test-cljs report-errors!]]
         '[danielsz.autoprefixer :refer [autoprefixer]]
         '[degree9.boot-npm :refer [npm]]
         '[degree9.boot-semver :refer :all]
         '[degree9.boot-semver.impl :refer [get-version]]
         '[degree9.boot-semgit :refer :all]
         '[funcool.boot-codeina :refer :all]
         '[hendrick.boot-medusa :refer :all]
         '[org.martinklepsch.boot-garden :refer [garden]]
         '[powerlaces.boot-cljs-devtools :refer [cljs-devtools]]
         '[pandeiro.boot-http :refer [serve]])


(task-options!
 apidoc {:title          (name +title+)
         :version        (get-version)
         :description    +description+
         :src-uri        (str +url+ "/tree/master/")
         :src-uri-prefix "#L"
         :sources        (get-env :source-paths)}
 pom {:project     +title+
      :description +description+
      :url         +url+
      :license     {"The MIT License (MIT)"
                    "http://opensource.org/licenses/mit-license.php"}}
 jar {:main     'ui.main
      :file     "ui.jar"
      :manifest {"Description" +description+}}
 repl {:middleware '[cemerick.piggieback/wrap-cljs-repl]}
 test-cljs {:js-env :phantom}
 target {:dir #{"target"}}
 autoprefixer {:exec-path "target/node_modules/postcss-cli/bin/postcss"
               :files     ["ui.css" "docs.css"]
               ;; We support all browsers that supports flex-box
               :browsers  "last 2 versions, Explorer >= 10, Android >= 4.1, Safari >= 7, iOS >= 7"})


(deftask readme
  "Generate a Clojure-common file from the README to be consumed client-side"
  []
  (let [content (str "(ns ui.readme)\n\n(def content \"" (slurp "README.md") "\")")
        out     "src/cljc/ui/readme.cljc"]
    (doto out (spit content))
    identity))


(deftask pre-requisits
  []
  (comp (npm :install {:postcss-cli  "latest"
                    :autoprefixer "latest"}
          :cache-key ::cache)
     (readme)
     (target)
     identity))


(deftask styles
  "Compile garden-styles and add browser-prefixes. Note that this
  requires that `postcss-cli` and `autoprefixer` is installed"
  []
  (comp (garden :output-to "css/ui.css"
             :styles-var 'ui.styles/screen)
     (garden :output-to "css/docs.css"
             :styles-var 'ui.styles/docs)
     (autoprefixer)
     identity))


(deftask dev
  []
  (comp (pre-requisits)
     (serve :handler 'afrey.ring-html5-handler/handler)
     (watch)
     (speak)
     (reload :on-jsload 'ui.core/mount-root
             :cljs-asset-path "")
     (styles)
     (cljs-repl)
     (cljs-devtools)
     (cljs :ids #{"ui"}
           :optimizations :none
           :source-map true
           :compiler-options {:asset-path      "/ui.out"
                              :preloads        '[devtools.preload
                                                 day8.re-frame.trace.preload]
                              :closure-defines {"re_frame.trace.trace_enabled_QMARK_" true}})))


(deftask prod
  []
  (comp (pre-requisits)
     (readme)
     (styles)
     (cljs :ids #{"ui"}
           :optimizations :advanced
           :compiler-options {:closure-defines    {"goog.DEBUG" false}
                              :language-in        :ecmascript5
                              :pretty-print       false
                              :static-fns         true
                              :optimize-constants true})))


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


(deftask deploy
  "Bump version and push to Github. Accepted pull-requests are
  automatically published to Clojars"
  [s speak?        bool "Notify when deployment is completed"
   b bump   VALUE  kw   "What to bump (major minor or patch)"]
  (comp (version bump 'inc)
     (git-push)))
