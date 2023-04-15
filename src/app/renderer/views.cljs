(ns app.renderer.views
  (:require
   [app.renderer.sci :refer [!points]]
   [app.renderer.sci-editor :as sci-editor]))


(defn main-panel []
    [:div 
     [sci-editor/editor "" !points {:eval? true}]])