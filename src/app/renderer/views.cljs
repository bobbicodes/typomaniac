(ns app.renderer.views
  (:require
   [app.renderer.sci :refer [!viewer]]
   [app.renderer.sci-editor :as sci-editor]))


(defn main-panel []
    [:div 
     [sci-editor/editor "" !viewer {:eval? true}]])