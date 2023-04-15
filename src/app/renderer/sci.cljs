(ns app.renderer.sci
  (:require ["@codemirror/view" :as view]
            [applied-science.js-interop :as j]
            [nextjournal.clojure-mode.extensions.eval-region :as eval-region]
            [sci.core :as sci]
            [reagent.core :as r]
            [sci.impl.evaluator]
            [clojure.string :as str]))

(defonce !viewer (r/atom ""))
(defonce last-result (r/atom ""))
 
(defonce context
  (sci/init {:classes {'js goog/global :allow :all}}))

(defn eval-string
  ([source] (eval-string context source))
  ([ctx source]
   (when-some [code (not-empty (str/trim source))]
     (try {:result (sci/eval-string* ctx code)}
          (catch js/Error e
            {:error (str (.-message e))})))))

(defonce eval-tail (atom nil))

(defn update-editor! [text cursor-pos]
  (let [end (count (some-> @!viewer .-state .-doc str))]
    (.dispatch @!viewer #js{:changes #js{:from 0 :to end :insert text}
                            :selection #js{:anchor cursor-pos :head cursor-pos}})))

(j/defn eval-at-cursor [on-result ^:js {:keys [state]}]
  (let [cursor-pos (some-> @!viewer .-state .-selection .-main .-head)
        code (first (str/split (str (some-> @!viewer .-state .-doc str)) #" => "))]
    (some->> (eval-region/cursor-node-string state)
             (eval-string)
             (on-result))
    (update-editor! (str (subs code 0 cursor-pos)
                         (when-not (= "" (:result @last-result)) " => ")
                         (:result @last-result)
                         (reset! eval-tail (subs code cursor-pos (count code))))
                    cursor-pos)
    (.dispatch @!viewer 
               #js{:selection #js{:anchor cursor-pos
                                  :head   cursor-pos}}))
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

(defn clear-eval []
  (when (not= "" @last-result)
    (reset! last-result "")
    (let [code (-> @!viewer
                   (some-> .-state .-doc str)
                   str
                   (str/split #" => ")
                   first
                   (str @eval-tail))
          cursor-pos (some-> @!viewer .-state .-selection .-main .-head)]
      (update-editor! code (min cursor-pos (count code))))))

(defn extension []
  (.of view/keymap
       (j/lit
        [{:key "Alt-Enter"
          :run (partial eval-cell (fn [result] (reset! last-result result)))}
         {:key  "Mod-Enter"
          :run (partial eval-top-level (fn [result] (reset! last-result result)))}
         {:key "Shift-Enter" 
          :run (partial eval-at-cursor (fn [result] (reset! last-result result)))}
         {:key "Escape" :run clear-eval}
         {:key "ArrowLeft" :run clear-eval}
         {:key "ArrowRight" :run clear-eval}])))