(ns app.renderer.sci
  (:require ["@codemirror/view" :as view]
            [app.renderer.max-or-throw :refer [max-or-throw]]
            [applied-science.js-interop :as j]
            [nextjournal.clojure-mode.extensions.eval-region :as eval-region]
            [sci.core :as sci]
            [reagent.core :as r]
            [sci.impl.evaluator]
            [clojure.string :as str]))

(defonce last-result (r/atom ""))

(def eval-result
  (r/atom ""))
 
(defonce context
  (sci/init {:classes {'js goog/global :allow :all}
             :namespaces {'max-or-throw.core {'max-or-throw max-or-throw}}}))

(defn eval-string [source]
  (try (sci/eval-string* context source)
       (catch :default e
         (str e))))

(defonce !points (r/atom ""))

(defonce eval-tail (atom nil))

(defn update-editor! [text cursor-pos]
  (let [end (count (some-> @!points .-state .-doc str))]
    (.dispatch @!points #js{:changes #js{:from 0 :to end :insert text}
                            :selection #js{:anchor cursor-pos :head cursor-pos}})))

(j/defn eval-at-cursor [on-result ^:js {:keys [state]}]
  (let [cursor-pos (some-> @!points .-state .-selection .-main .-head)
        code (first (str/split (str (some-> @!points .-state .-doc str)) #" => "))]
    (some->> (eval-region/cursor-node-string state)
             (eval-string)
             (on-result))
    (update-editor! (str (subs code 0 cursor-pos)
                         (when-not (= "" @last-result) " => ")
                         @last-result
                         (reset! eval-tail (subs code cursor-pos (count code))))
                    cursor-pos)
    (.dispatch @!points #js{:selection #js{:anchor cursor-pos :head cursor-pos}}))
  true)

(j/defn eval-top-level [on-result ^:js {:keys [state]}]
  (some->> (eval-region/top-level-string state)
           (eval-string)
           (on-result))
  true)

(j/defn eval-cell [on-result ^:js {:keys [state]}]
  (-> (str "(do " (.-doc state) " )")
      (eval-string)
      (on-result))
  true)

(defn keymap* [modifier]
  {:eval-cell
   [{:key (str modifier "-Enter")
     :doc "Evaluate cell"}]
   :eval-at-cursor
   [{:key "Shift-Enter"
     :doc "Evaluates form at cursor"}]
   :eval-top-level
   [{:key "Mod-Enter"
     :doc "Evaluates top-level form at cursor"}]})

(defn extension [{:keys [modifier on-result]}]
  (.of view/keymap
       (j/lit
        [{:key (str modifier "-Enter")
          :run (partial eval-cell on-result)}
         {:key  "Mod-Enter"
          :run (partial eval-top-level on-result)}
         {:key "Enter"
          :shift (partial eval-at-cursor on-result)
          :run #()}])))