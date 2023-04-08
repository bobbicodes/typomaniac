(ns app.renderer.events
  (:require
   [re-frame.core :as re-frame]
   [re-pressed.core :as rp]
   [app.renderer.db :as db]
   [app.renderer.subs :as subs]))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))
