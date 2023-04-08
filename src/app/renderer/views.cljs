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
    {:event-keys [[[::events/set-current-key " "] [{:keyCode 32}]]
                  [[::events/set-current-key "A"] [{:keyCode 65 :shiftKey true}]]
                  [[::events/set-current-key "B"] [{:keyCode 66 :shiftKey true}]]
                  [[::events/set-current-key "C"] [{:keyCode 67 :shiftKey true}]]
                  [[::events/set-current-key "D"] [{:keyCode 68 :shiftKey true}]]
                  [[::events/set-current-key "E"] [{:keyCode 69 :shiftKey true}]]
                  [[::events/set-current-key "F"] [{:keyCode 70 :shiftKey true}]]
                  [[::events/set-current-key "G"] [{:keyCode 71 :shiftKey true}]]
                  [[::events/set-current-key "H"] [{:keyCode 72 :shiftKey true}]]
                  [[::events/set-current-key "I"] [{:keyCode 73 :shiftKey true}]]
                  [[::events/set-current-key "J"] [{:keyCode 74 :shiftKey true}]]
                  [[::events/set-current-key "K"] [{:keyCode 75 :shiftKey true}]]
                  [[::events/set-current-key "L"] [{:keyCode 76 :shiftKey true}]]
                  [[::events/set-current-key "M"] [{:keyCode 77 :shiftKey true}]]
                  [[::events/set-current-key "N"] [{:keyCode 78 :shiftKey true}]]
                  [[::events/set-current-key "O"] [{:keyCode 79 :shiftKey true}]]
                  [[::events/set-current-key "P"] [{:keyCode 80 :shiftKey true}]]
                  [[::events/set-current-key "Q"] [{:keyCode 81 :shiftKey true}]]
                  [[::events/set-current-key "R"] [{:keyCode 82 :shiftKey true}]]
                  [[::events/set-current-key "S"] [{:keyCode 83 :shiftKey true}]]
                  [[::events/set-current-key "T"] [{:keyCode 84 :shiftKey true}]]
                  [[::events/set-current-key "U"] [{:keyCode 85 :shiftKey true}]]
                  [[::events/set-current-key "V"] [{:keyCode 86 :shiftKey true}]]
                  [[::events/set-current-key "W"] [{:keyCode 87 :shiftKey true}]]
                  [[::events/set-current-key "X"] [{:keyCode 88 :shiftKey true}]]
                  [[::events/set-current-key "Y"] [{:keyCode 89 :shiftKey true}]]
                  [[::events/set-current-key "Z"] [{:keyCode 90 :shiftKey true}]]
                  [[::events/set-current-key "a"] [{:keyCode 65}]]
                  [[::events/set-current-key "b"] [{:keyCode 66}]]
                  [[::events/set-current-key "c"] [{:keyCode 67}]]
                  [[::events/set-current-key "d"] [{:keyCode 68}]]
                  [[::events/set-current-key "e"] [{:keyCode 69}]]
                  [[::events/set-current-key "f"] [{:keyCode 70}]]
                  [[::events/set-current-key "g"] [{:keyCode 71}]]
                  [[::events/set-current-key "h"] [{:keyCode 72}]]
                  [[::events/set-current-key "i"] [{:keyCode 73}]]
                  [[::events/set-current-key "j"] [{:keyCode 74}]]
                  [[::events/set-current-key "k"] [{:keyCode 75}]]
                  [[::events/set-current-key "l"] [{:keyCode 76}]]
                  [[::events/set-current-key "m"] [{:keyCode 77}]]
                  [[::events/set-current-key "n"] [{:keyCode 78}]]
                  [[::events/set-current-key "o"] [{:keyCode 79}]]
                  [[::events/set-current-key "p"] [{:keyCode 80}]]
                  [[::events/set-current-key "q"] [{:keyCode 81}]]
                  [[::events/set-current-key "r"] [{:keyCode 82}]]
                  [[::events/set-current-key "s"] [{:keyCode 83}]]
                  [[::events/set-current-key "t"] [{:keyCode 84}]]
                  [[::events/set-current-key "u"] [{:keyCode 85}]]
                  [[::events/set-current-key "v"] [{:keyCode 86}]]
                  [[::events/set-current-key "w"] [{:keyCode 87}]]
                  [[::events/set-current-key "x"] [{:keyCode 88}]]
                  [[::events/set-current-key "y"] [{:keyCode 89}]]
                  [[::events/set-current-key "z"] [{:keyCode 90}]]
                  [[::events/set-current-key "-"] [{:keyCode 189}]]
                  [[::events/set-current-key "'"] [{:keyCode 222}]]]

     :clear-keys
     [[{:keyCode 27} ;; escape
       ]]
     :prevent-default-keys
     [;; Ctrl+g
      {:keyCode   32}]
    ;; is pressed
     }]))

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
   [sci-editor/editor demo !points {:eval? false}]
   ;[key-bindings-table (merge keymap/paredit-keymap* (app.renderer.sci/keymap* "Alt"))]
   ])
