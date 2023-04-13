(ns app.renderer.events
  (:require
   [re-frame.core :as re-frame]
   [app.renderer.db :as db]
   [clojure.string :as str]
   [app.renderer.sci :refer [clear-eval]]))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(re-frame/reg-event-db
 ::clear-result
 (fn [db [_]]
   (clear-eval)
   (assoc db :eval-result "")))
