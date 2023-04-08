(ns app.renderer.views
  (:require
   [re-frame.core :as re-frame]
   [re-pressed.core :as rp]
   [app.renderer.events :as events]
   [app.renderer.subs :as subs]
   [app.renderer.sci :refer [!points update-editor!]]
   [app.renderer.sci-editor :as sci-editor :refer [points !result]]
   [nextjournal.clojure-mode.keymap :as keymap]
   [goog.object :as o]
   [clojure.string :as str]))

(defn dispatch-keydown-rules []
  (re-frame/dispatch
    [::rp/set-keydown-rules
     {:event-keys
      (conj (into []
                  (for [n (into [8 27 32 37 38 40] (range 40 100))]
                    [[::events/clear-result]
                     [{:keyCode n}]]))
            [[::events/cursor-right] [{:keyCode 39}]])
      :always-listen-keys [{:keyCode   13 :shiftKey true}]
      :prevent-default-keys [{:keyCode   13 :shiftKey true}]}]))

(dispatch-keydown-rules)

(def demo "(map inc (range 8))")

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
              #(update-editor! (-> % .-target .-result)))))}])

(defn main-panel []
  [:div 
   [load]
   [:button
    {:on-click #(let [file-blob (js/Blob. [(str (some-> @!points .-state .-doc str))] #js {"type" "text/plain"})
                      link (.createElement js/document "a")]
                  (set! (.-href link) (.createObjectURL js/URL file-blob))
                  (.setAttribute link "download" "mecca.txt")
                  (.appendChild (.-body js/document) link)
                  (.click link)
                  (.removeChild (.-body js/document) link))}
    "Save"]
   [sci-editor/editor demo !points {:eval? true}]
   ;[key-bindings-table (merge keymap/paredit-keymap* (app.renderer.sci/keymap* "Alt"))]
   ])
