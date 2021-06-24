(ns typing.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::text
 (fn [db]
   (:text db)))

(re-frame/reg-sub
 ::text2
 (fn [db]
   (:text2 db)))

(re-frame/reg-sub
 ::cursor-pos
 (fn [db]
   (:cursor-pos db)))

(re-frame/reg-sub
 ::times
 (fn [db]
   (vec (sort (keys (:presses db))))))

(re-frame/reg-sub
 ::presses
 (fn [db]
   (:presses db)))

(re-frame/reg-sub
 ::prob-keys
 (fn [db]
   (:prob-keys db)))

(re-frame/reg-sub
 ::deltas
 (fn [db]
   (let [presses (reverse (reverse (vec (sort (keys (:presses db))))))]
     (remove #(> % 1000)
             (for [x (range (dec (count presses)))]
               (- (nth presses (inc x))
                  (nth presses x)))))))

(re-frame/reg-sub
 ::ave-wpm
 (fn [db]
   (let [presses (reverse (take 50 (reverse (vec (sort (keys (:presses db)))))))
         deltas (remove #(> % 5000)
                        (for [x (range (dec (count presses)))]
                          (- (nth presses (inc x))
                             (nth presses x))))]
     (.round js/Math  (* 60 (/ (/ 1000 (/ (reduce + deltas)
                                             (count deltas)))
                                  5))))))

(re-frame/reg-sub
 ::moving-ave
 (fn [db]
   (let [presses (reverse (take 50 (reverse (vec (sort (keys (:presses db)))))))
         deltas (remove #(> % 5000)
                        (for [x (range (dec (count presses)))]
                          (- (nth presses (inc x))
                             (nth presses x))))]
     (.round js/Math  (* 60 (/ (/ 1000 (/ (reduce + deltas)
                                          (count deltas)))
                               5))))))

(re-frame/reg-sub
 ::all-time-ave
 (fn [db]
   (let [presses (vec (sort (keys (:presses db))))
         deltas (remove #(> % 5000)
                        (for [x (range (dec (count presses)))]
                          (- (nth presses (inc x))
                             (nth presses x))))]
     (.round js/Math  (* 60 (/ (/ 1000 (/ (reduce + deltas)
                                          (count deltas)))
                               5))))))

(re-frame/reg-sub
 ::total-time
 (fn [db]
   (let [presses (vec (sort (keys (:presses db))))
         deltas (remove #(> % 5000)
                        (for [x (range (dec (count presses)))]
                          (- (nth presses (inc x))
                             (nth presses x))))
         ms (reduce + deltas)]
     (.round js/Math (/ ms 1000)))))

(re-frame/reg-sub
 ::current-key
 (fn [db]
   (:current-key db)))

(re-frame/reg-sub
 ::re-pressed-example
 (fn [db _]
   (:re-pressed-example db)))
