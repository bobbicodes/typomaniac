(ns app.renderer.core
  (:require [reagent.dom :as rd]
            [app.renderer.views :as views]))

(enable-console-print!)

(defn root-component []
  [views/main-panel])

(defn ^:dev/after-load start! []
  (rd/render
   [root-component]
   (js/document.getElementById "app-container")))
