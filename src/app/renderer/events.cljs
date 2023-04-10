(ns app.renderer.events
  (:require
   [re-frame.core :as re-frame]
   [app.renderer.db :as db]
   [clojure.string :as str]
   [app.renderer.sci :refer [!points last-result update-editor! eval-tail]]))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(re-frame/reg-event-db
 ::clear-result
 (fn [db [_]]
   (when (not= "" @last-result)
     (reset! last-result "")
     (let [code (str (first (str/split (str (some-> @!points .-state .-doc str)) #" => ")) @eval-tail)
           cursor-pos (some-> @!points .-state .-selection .-main .-head)]
       (update-editor! code (min cursor-pos (count code)))))
   (assoc db :eval-result "")))
