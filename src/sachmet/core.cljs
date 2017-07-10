(ns sachmet.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(enable-console-print!)

;;state safe via global atom
(def app-state (atom {:count 0}))


;;parser Read Function. signature: [env key params]
;;query expressions = always a vector -> parsing it = a map
(defn read
  [{:keys [state] :as env} key params]
  (let [st @state]
    (if-let [[_ v] (find st key)]
      {:value v}
      {:value :not-found})))

;;parser mutation function. signature: identical to read function 
;; :key vector -> what reaad operation should follow the mutation
;; :action = function with no argumentts 

(defn mutate
  [{:keys [state] :as env} key params]
  (cond
        (= 'increment key) {:value {:keys [:count]} :action #(swap! state update-in [:count] inc)}
        (= 'decrement key) {:value {:keys [:count]} :action #(swap! state update-in [:count] dec)}
        (= 'reset key) {:value {:keys [:count]} :action #(reset! state)}
        :else {:value :not-found}))

;;parsing involves 2 kinds of expressions reads and mutations
;;takes a query expression and evals it using the provided read and mutate implementations
;;(def my-parser (om/parser {:read read}))

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
  static om/IQuery
  (query [this]
    [:count])
  Object
  (render [this]
    (let [{:keys [count]} (om/props this)]
      (dom/div nil
        (dom/span nil (str "Number: " count "  "))
        (dom/button
         #js {:onClick
              (fn [e] (om/transact! this '[(increment)]))}
          "inc")
        (dom/button
         #js {:onClick
              (fn [e] (om/transact! this '[(decrement)]))}
          "dec")
        (dom/button
          #js {:onClick
                 (fn [e] (om/transact! this '[(reset)]))}
          "reset")
        ))))


(def reconciler
  (om/reconciler
   {:state app-state
    :parser (om/parser {:read read :mutate mutate})}))

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
