(ns clj-pdf-for-dummies.core
  (:require [clj-pdf.core :as pdf]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.java.io :as io])
  (:gen-class))

(def page-size :letter)
(def page-width 612)
(def page-height 792)
(def margin-left 30)
(def margin-right 30)

(defn cover
  [])

(defn -main
  "Generates the pdf at specified location (defaults to `output/clj-pdf-for-dummies.pdf`)."
  [& args]
  (let [{{:keys [dest]} :options}
        (parse-opts args
                    [["-d" "--dest DEST" "Destination to output the pdf to."
                      :default "output/clj-pdf-for-dummies.pdf"]])]
    (io/make-parents dest)
    (pdf/pdf
     [{:size page-size
       :left-margin margin-left
       :right-magin margin-right
       :letterhead (cover)
       :footer {:start-page 2 :align :center}}]
     dest)
    (println "Generated pdf at " dest)))
