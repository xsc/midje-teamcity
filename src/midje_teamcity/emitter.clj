(ns ^{:doc "TeamCity emitter for Midje"
      :author "Yannick Scherer"}
  midje-teamcity.emitter
  (:require [midje.emission.util :refer :all]
            [midje.emission.state :as state]
            [midje.emission.plugins.util :as util]
            [midje.emission.plugins.default :as default]
            [midje.emission.plugins.silence :as silence]
            [midje.data.fact :as fact]
            [midje.config :as config]
            [clojure.string :as str]))

(config/change-defaults :print-level :print-facts)

;; ## TeamCity Output

(defn- escape-str
  [s]
  (clojure.string/replace
    (str s) #"['|\n\r\[\]]"
    (fn [x]
      (cond (= x "\n") "|n"
            (= x "\r") "|r"
            :else (str "|" x)))))

(defn- ->tc
  [attrs]
  (if (seq (rest attrs))
    (->> attrs
         (partition 2)
         (map (fn [[n v]] (str (name n) "='" (escape-str v) "'")))
         (str/join " "))
    (str "'" (first attrs) "'")))

(defn- ->tc-msg
  [message attrs]
  (str "##teamcity[" (name message)
       (when (seq attrs) (str " " (->tc attrs)))
       "]"))

(defn- emit-tc
  [message & attrs]
  (util/emit-one-line (->tc-msg message attrs)))

;; ## State

(defonce last-fact (atom nil))
(defonce last-ns (atom nil))

;; ## Helpers

(defn- fact-name
  [fact]
  (or (fact/name fact)
      (fact/description fact)
      (str (fact/file fact) ":" (fact/line fact))))

;; ## Handlers

(defn starting-fact-stream
  []
  (reset! last-fact nil)
  (reset! last-ns nil))

(defn finishing-fact-stream
  [result x]
  (when @last-ns
    (emit-tc :testSuiteFinished :name @last-ns)))

(defn finishing-fact
  [fact]
  (when-not (-> fact meta :midje/table-bindings)
    (let [{:keys [failure]} @last-fact
          name (fact-name fact)]
      (if failure
        (emit-tc :testFailed :name name)
        (emit-tc :testFinished :name name)))))

(defn starting-to-check-fact [fact]
  (let [fact-namespace (str (fact/namespace fact))
        last-namespace @last-ns]

    (when (not= fact-namespace last-namespace)
      (reset! last-ns fact-namespace)
      (when last-namespace
        (emit-tc :testSuiteFinished :name last-namespace))
      (emit-tc :testSuiteStarted :name fact-namespace))

    (when-not (-> fact meta :midje/table-bindings)
      (let [nm (fact-name fact)]
        (emit-tc :testStarted :name nm :captureStandardOutput true)
        (swap! last-fact #(assoc % :name nm :top-name (:name %)))))))

(defn fail
  [f]
  (default/fail f)
  (swap! last-fact assoc :failure f))

;; ## Register

(defn make-map [& keys]
  (zipmap keys
          (map #(ns-resolve *ns* (symbol (name %))) keys)))

(def emission-map
  (->> (make-map
         :fail
         :finishing-fact
         :finishing-fact-stream
         :starting-fact-stream
         :starting-to-check-fact)
       (merge silence/emission-map)))

(state/install-emission-map emission-map)
