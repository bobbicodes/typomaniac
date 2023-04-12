(ns app.renderer.views
  (:require
   [reagent.core :as r]
   [re-frame.core :as re-frame]
   [re-pressed.core :as rp]
   [app.renderer.events :as events]
   [app.renderer.sci :refer [update-editor! files file]]
   [app.renderer.sci-editor :as sci-editor]
   [nextjournal.clojure-mode.keymap :as keymap]
   [goog.object :as o]
   [clojure.string :as str]
   [goog.string :as gstring]))

(re-frame/dispatch [::rp/set-keydown-rules
                    {:event-keys (into [] (for [n (into [8 27 32 37 38 39 40] (range 40 100))]
                                            [[::events/clear-result]
                                             [{:keyCode n}]]))}])

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

(defn tab [s x color]
  [:g [:rect {:x      x
              :y      0.5
              :width  (+ 2 (* 1.5 (count s)))
              :height 28
              :cursor "pointer"
              :fill   color}]
   [text s x 0 (if (= color "purple") "white" "black")]])

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
      [:svg {:width "100%" :view-box "0 0 100 5"}
       [tab "t" 0 "#F8B0F8"]
       [tab "test" 4 "violet"]
       [tab "untitled3.clj" 12.5 "violet"]
       [tab "very_long_file_name_foo.txt" 34.5 "violet"]]
     (into [:div]
           (for [n (range (count @files))]
             [sci-editor/editor "" (:viewer (get @files n)) {:eval? true
                                                   :visible? (if (= n @file) true false)}]))
     (when @key-bindings?
       [key-bindings-table (merge keymap/paredit-keymap* (app.renderer.sci/keymap* "Alt"))])])