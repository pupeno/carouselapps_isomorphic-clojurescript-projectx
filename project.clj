(defproject projectx "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [ring-server "0.4.0"]
                 [cljsjs/react "0.13.3-1"]
                 [reagent "0.5.1"]
                 [reagent-forms "0.5.9"]
                 [reagent-utils "0.1.5"]
                 [ring "1.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [prone "0.8.2"]
                 [compojure "1.4.0"]
                 [hiccup "1.0.5"]
                 [environ "1.0.1"]
                 [org.clojure/clojurescript "1.7.48"]
                 [bidi "1.21.0"]
                 [kibu/pushy "0.3.3"]
                 [aleph "0.4.0"]]

  :plugins [[lein-environ "1.0.0"]
            [lein-asset-minifier "0.2.2"]]

  :ring {:handler      projectx.handler/app
         :uberwar-name "projectx.war"}

  :min-lein-version "2.5.0"

  :uberjar-name "projectx.jar"

  :main projectx.server

  :clean-targets ^{:protect false} [:target-path
                                    [:cljsbuild :builds :app :compiler :output-dir]
                                    [:cljsbuild :builds :app :compiler :output-to]]

  :source-paths ["src/clj" "src/cljc"]

  :minify-assets {:assets {"resources/public/css/site.min.css" "resources/public/css/site.css"}}

  :cljsbuild {:builds {:app         {:source-paths ["src/cljs" "src/cljc"]
                                     :compiler     {:output-to  "resources/public/js/app.js"
                                                    :output-dir "resources/public/js/app"}}
                       :server-side {:source-paths ["src/cljs" "src/cljc"]
                                     :compiler     {:output-to     "resources/public/js/server-side.js"
                                                    :output-dir    "resources/public/js/server-side"
                                                    :asset-path    "js/server-side"
                                                    :optimizations :whitespace}}}}

  :profiles {:dev     {:repl-options {:init-ns projectx.repl}

                       :dependencies [[ring/ring-mock "0.3.0"]
                                      [ring/ring-devel "1.4.0"]
                                      [lein-figwheel "0.3.9"]
                                      [org.clojure/tools.nrepl "0.2.11"]
                                      [pjstadig/humane-test-output "0.7.0"]]

                       :source-paths ["env/dev/clj"]
                       :plugins      [[lein-figwheel "0.3.9"]
                                      [lein-cljsbuild "1.0.6"]]

                       :injections   [(require 'pjstadig.humane-test-output)
                                      (pjstadig.humane-test-output/activate!)]

                       :figwheel     {:http-server-root "public"
                                      :server-port      3449
                                      :nrepl-port       7002
                                      :css-dirs         ["resources/public/css"]
                                      :ring-handler     projectx.handler/app}

                       :env          {:dev true}

                       :cljsbuild    {:builds {:app         {:source-paths ["env/dev/cljs"]
                                                             :compiler     {:optimizations :none
                                                                            :source-map    true
                                                                            :pretty-print  true
                                                                            :main          "projectx.dev"
                                                                            :verbose       true}}
                                               :server-side {:compiler {:optimizations :whitespace
                                                                        :source-map    "resources/public/js/server-side.js.map"
                                                                        :pretty-print  true
                                                                        :verbose       true}}}}}

             :uberjar {:hooks       [leiningen.cljsbuild minify-assets.plugin/hooks]
                       :env         {:production true}
                       :aot         :all
                       :omit-source true
                       :cljsbuild   {:jar    true
                                     :builds {:app         {:source-paths ["env/prod/cljs"]
                                                            :compiler     {:optimizations :advanced
                                                                           :pretty-print  false}}
                                              :server-side {:compiler {:optimizations :advanced
                                                                       :pretty-print  false}}}}}})
