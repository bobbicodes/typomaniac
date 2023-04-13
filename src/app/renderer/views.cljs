(ns app.renderer.views
  (:require
   [reagent.core :as r]
   [app.renderer.sci :refer [update-editor! files file update-editor!]]
   [app.renderer.sci-editor :as sci-editor]
   [nextjournal.clojure-mode.keymap :as keymap]
   [goog.object :as o]
   [clojure.string :as str]))

(defn linux? []
  (some? (re-find #"(Linux)|(X11)" js/navigator.userAgent)))

(defn mac? []
  (and (not (linux?))
       (some? (re-find #"(Mac)|(iPhone)|(iPad)|(iPod)" js/navigator.platform))))

(defn key-mapping []
  (cond-> {"ArrowUp" "↑"
           "ArrowDown" "↓"
           "ArrowRight" "→"
           "ArrowLeft" "←"
           "Mod" "Ctrl"}
    (mac?)
    (merge {"Alt" "⌥"
            "Shift" "⇧"
            "Enter" "⏎"
            "Ctrl" "⌃"
            "Mod" "⌘"})))

(defn render-key [key]
  (let [keys (into [] (map #(get ((memoize key-mapping)) % %) (str/split key #"-")))]
    (into [:span]
          (map-indexed (fn [i k]
                         [:<>
                          (when-not (zero? i) [:span " + "])
                          [:kbd.kbd k]]) keys))))

(defn key-bindings-table [keymap]
  [:table.w-full.text-sm
   [:thead
    [:tr.border-t
     [:th.px-3.py-1.align-top.text-left.text-xs.uppercase.font-normal.black-50 "Command"]
     [:th.px-3.py-1.align-top.text-left.text-xs.uppercase.font-normal.black-50 "Keybinding"]
     [:th.px-3.py-1.align-top.text-left.text-xs.uppercase.font-normal.black-50 "Alternate Binding"]
     [:th.px-3.py-1.align-top.text-left.text-xs.uppercase.font-normal.black-50 {:style {:min-width 290}} "Description"]]]
   (into [:tbody]
         (->> keymap
              (sort-by first)
              (map (fn [[command [{:keys [key shift doc]} & [{alternate-key :key}]]]]
                     [:<>
                      [:tr.border-t.hover:bg-gray-100
                       [:td.px-3.py-1.align-top.monospace.whitespace-nowrap [:b (name command)]]
                       [:td.px-3.py-1.align-top.text-right.text-sm.whitespace-nowrap (render-key key)]
                       [:td.px-3.py-1.align-top.text-right.text-sm.whitespace-nowrap (some-> alternate-key render-key)]
                       [:td.px-3.py-1.align-top doc]]
                      (when shift
                        [:tr.border-t.hover:bg-gray-100
                         [:td.px-3.py-1.align-top [:b (name shift)]]
                         [:td.px-3.py-1.align-top.text-sm.whitespace-nowrap.text-right
                          (render-key (str "Shift-" key))]
                         [:td.px-3.py-1.align-top.text-sm]
                         [:td.px-3.py-1.align-top]])]))))])

(defn load []
  [:input#input
   {:type      "file"
    :on-change
    (fn [e]
      (let [dom    (o/get e "target")
            file   (o/getValueByKeys dom #js ["files" 0])
            reader (js/FileReader.)]
        (.readAsText reader file)
        (set! (.-onload reader)
              #(update-editor! (str/trim (-> % .-target .-result)) 0))))}])

(def key-bindings? (r/atom false))

(defn button [label on-click color]
  [:button
   {:style {:color "white"
            :padding "4px"
            :background-color color
            :background-image "linear-gradient(to top left,
             rgba(0, 0, 0, .2),
             rgba(0, 0, 0, .2) 30%,
             rgba(0, 0, 0, 0))"
            :font-size "14px"
            :text-shadow "1px 1px 1px #000"
            :border-radius "10px"
            :box-shadow "inset 2px 2px 3px rgba(255, 255, 255, .6),
             inset -2px -2px 3px rgba(0, 0, 0, .6)"}
    :on-click on-click}
   label])

(defn new-file []
  (swap! files conj {:filename (str "untitled" (+ 2 @file) ".clj") :viewer (r/atom "")})
  (swap! file inc))

(defn text
  [name x y color]
  [:text {:id name
          :x (inc x) :y (inc y) :dy "0.9em"  :text-anchor "left"
          :font-family "monospace"
          :pointer-events "none"
          :font-size "2.5px"
          :fill color} name])

(defn tab [index s x color]
      (let [width (max 7 (+ 3 (* 1.5 (count s))))
            hover (r/atom nil)]
        (fn []
          [:g [:rect {:x      x :y      0.5
                      :width width :height 28
                      :cursor "pointer" :fill   color}]
         ;; tab outline
           [:path {:d (str "M" (+ x 0.1) " 5v -4.5h" width " v5") 
                   :stroke "black" :stroke-width 0.125 :fill "none"}]
           [text s x 0 (if (= color "purple") "white" "black")]
         ;; box appears on mouseover
           (when (= index @hover)
             [:rect {:x (+ width x -2.5) :y 0.5 :width 2.5 :height 2.5
                     :fill "red" :stroke-width 0.1 :stroke "black"}])
         ;; "X" in corner of tab to close
           [:path {:transform (str "translate(" (+ width x -2.8) "," 0 ")")
                   :d "M1 1L2.5 2.5M2.5 1L1 2.5" :stroke "black" :stroke-width (if (= index @hover) 0.3 0.1)}]
         ;; hover/click target
           [:rect {:x (+ width x -3) :y 0.5 :width 3 :height 3 :visibility "hidden"
                   :pointer-events "all"
                   :on-mouse-over #(reset! hover index)  
                   :on-mouse-out #(reset! hover nil)}]])))

(defn main-panel []
    [:div 
     #_[:div [button "New" #(new-file) "violet"]
      [button "Open" #() "violet"]
      [button "Options" #() "violet"]
      [button "Save"
       #(let [file-blob (js/Blob. [(str (some-> (deref (:viewer (@files @file))) .-state .-doc str))] #js {"type" "text/plain"})
              link      (.createElement js/document "a")]
          (set! (.-href link) (.createObjectURL js/URL file-blob))
          (.setAttribute link "download" (:filename (@files @file)))
          (.appendChild (.-body js/document) link)
          (.click link)
          (.removeChild (.-body js/document) link)) "violet"]
      (gstring/unescapeEntities "&emsp;&emsp;")
      (into [:span]
            (for [n (range (count @files))]
              [button (:filename (get @files n)) #(reset! file n) (if (= n @file) "purple" "violet")]))]
      [:svg {:width 600 :view-box "0 0 100 5"}
       [tab 0 "t" 0 "#F8B0F8"]
       [tab 1 "test" 7 "violet"]
       [tab 2 "untitled3.clj" 16 "violet"]
       [tab 3 "very_long_file_name_foo.txt" 38.5 "violet"]]
     (into [:div]
           (for [n (range (count @files))]
             [sci-editor/editor "" (:viewer (get @files n)) {:eval? true
                                                   :visible? (if (= n @file) true false)}]))
     (when @key-bindings?
       [key-bindings-table (merge keymap/paredit-keymap* (app.renderer.sci/keymap* "Alt"))])])