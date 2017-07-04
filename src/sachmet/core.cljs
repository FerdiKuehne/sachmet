(ns sachmet.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(enable-console-print!)

;;state safe via global atom
(def app-state (atom {:count 0}))

;;syntax for declaring om Objects (JavaScriptClass) without parameterizing
(defui intro
  Object
  (render [this]
    (dom/div nil
             (dom/h1 nil "God's of the Ancient Egypt"))))

;;with parameterizing
(defui Amun
  Object
  (render [this]
    (dom/div nil
             (dom/h3 nil (get (om/props this) :title))
             (dom/p nil  (get (om/props this) :text)))))

;;new syntax for state components
(defui Counter
  Object
  (render [this]
    (let [{:keys [count]} (om/props this)]
      (dom/div nil
        (dom/span nil (str "Number: " count "  "))
        (dom/button
          #js {:onClick
               (fn [e]
                 (swap! app-state update-in [:count] inc))}
          "inc")
        (dom/button
          #js {:onClick
               (fn [e]
                 (swap! app-state update-in [:count] dec))}
          "dec")
        (dom/button
          #js {:onClick
               (fn [e]
                 (reset! app-state))}
          "reset")
        ))))


(def reconciler
  (om/reconciler {:state app-state}))

;;producing factory from objects
(def introGods (om/factory intro))

(def iAmun (om/factory Amun))

;;render return om next or react component -> introGods return div components /stateless
(js/ReactDOM.render
 (introGods)
 (gdom/getElement "app0"))

;;components no longer hard codes a specific string /stateless
(js/ReactDOM.render
 ;;some alteration
 (apply dom/div nil
        (iAmun {:title "Amun"})
        (map (fn [words number] (iAmun {:react-key number
                               :text (str number " " words)}))
             ["A" "Man" "with" "Wind"]
             (range 1 5)))
             (gdom/getElement "app1"))

;;Adding State via global atom
(om/add-root! reconciler
  Counter (gdom/getElement "app2"))
