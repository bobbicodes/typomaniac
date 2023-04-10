(ns app.renderer.events
  (:require
   [reagent.core :as r]
   [re-frame.core :as re-frame]
   [re-pressed.core :as rp]
   [app.renderer.db :as db]
   [clojure.string :as str]
    [app.renderer.sci-editor :as sci-editor :refer [eval-all]]
   [app.renderer.sci :refer [!points last-result update-editor! eval-tail]]
   [app.renderer.subs :as subs]))

(defonce !debug (r/atom ""))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(re-frame/reg-event-db
 ::clear-result
 (fn [db [_]]
   (when (not= "" @last-result)
     (reset! last-result "")
     (update-editor! (str (first (str/split (str (some-> @!points .-state .-doc str)) #" => ")) @eval-tail)
                    (some-> @!points .-state .-selection .-main .-head)))
   (assoc db :eval-result "")))