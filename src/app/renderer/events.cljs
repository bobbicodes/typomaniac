(ns app.renderer.events
  (:require
   [re-frame.core :as re-frame]
   [re-pressed.core :as rp]
   [app.renderer.db :as db]
   [clojure.string :as str]
    [app.renderer.sci-editor :as sci-editor :refer [eval-all]]
   [app.renderer.sci :refer [!points last-result update-editor! eval-tail]]
   [app.renderer.subs :as subs]))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(re-frame/reg-event-db
 ::set-result
 (fn [db [_ s]]
   (update-editor! (str (first (str/split (str (some-> @!points .-state .-doc str)) #" => "))
                        (when-not (= "" @last-result) " => ") @last-result))
   (assoc db :eval-result (str (eval-all (str (some-> @!points .-state .-doc str)))))))

(re-frame/reg-event-db
 ::clear-result
 (fn [db [_]]
   (when (not= "" @last-result)
     (reset! last-result "")
     (update-editor! (str (first (str/split (str (some-> @!points .-state .-doc str)) #" => ")) @eval-tail)))
   (reset! eval-tail "")
   (assoc db :eval-result "")))