(ns clj-pdf-for-dummies.core
  (:require [clj-pdf.core :as pdf]
            [hiccup.core :refer [html]]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.java.io :as io]
            [clojure.string :as str])
  (:gen-class))

; -- Fonts ---------------------------------------------------------------------
; register clj-pdf fonts:
(clj-pdf.graphics-2d/g2d-register-fonts [["resources/fonts" false]])
; create java.awt.Font instances for use with [:graphics]:
(def special-elite-font (java.awt.Font/createFont
                          java.awt.Font/TRUETYPE_FONT
                          (-> "fonts/SpecialElite-Regular.ttf" io/resource io/input-stream)))

(def lato-bold-italic (java.awt.Font/createFont
                        java.awt.Font/TRUETYPE_FONT
                        (-> "fonts/Lato-BoldItalic.ttf" io/resource io/input-stream)))

(defn resize-font
 [font size]
 (.deriveFont font (float size)))

(defn center-font-x
  [g2d width font string]
  (let [font-metrics (.getFontMetrics g2d font)]
    (int (- (/ width 2)
            (/ (.stringWidth font-metrics string) 2)))))

; -- PDF Sizes -
(def page-size :letter)
(def page-width 612)
(def page-height 792)
(def margin-left 30)
(def margin-right 30)

; -- Colors -------------------------------------------------------------------
(def black "#000000")
(def white "#ffffff")
(def yellow "#FCEE37")
(def turquoise "#1ABC9C")

(defn hex->rgb-int
  "Given a hexadecimal string (starting with `#`) representing a color, returns
  the corresponding RGB integer."
  [c]
  (-> c
      (str/split #"#")
      rest
      first
      (Integer/parseInt 16)))

; -- Helpers -------------------------------------------------------------------
(defn svg
  "Returns a properly formatted SVG string with given attributes and body.
  Meant to be used inside `clj-pdf`'s `[:svg]` element."
  [attrs & body]
  (html
   (into
    [:svg (merge {:xmlns "http://www.w3.org/2000/svg"} attrs)]
    body)))

; -- Pages ---------------------------------------------------------------------
(defn cover-text
 [g2d width]
 (let [t1 "clj-pdf"
       t2 "FOR"
       t3 "DUMMIES"
       center (partial center-font-x g2d width)
       special-elite-16 (resize-font special-elite-font 16)
       special-elite-50 (resize-font special-elite-font 50)
       lato-bold-italic-40 (resize-font lato-bold-italic 40)]
   (doto g2d
    (.setColor java.awt.Color/WHITE)
    ; draw first string
    (.setFont lato-bold-italic-40)
    (.drawString t1 (center lato-bold-italic-40 t1) 50)

    ; draw second string
    (.setFont special-elite-16)
    (.drawString t2 (center special-elite-16 t2) 100)

    ; draw third string
    (.setFont special-elite-50)
    (.drawString t3 (center special-elite-50 t3) 170))))

(defn cover
  []
  [[:graphics {:under true}
    (fn [g2d]
      (doto g2d
        (.setColor (-> yellow hex->rgb-int java.awt.Color.))
        (.fillRect 0 0 page-width page-height)))]
   [:graphics {:translate [0 100] :rotate -0.05}
    (fn [g2d]
     (let [width (+ page-width 50)]
       (doto g2d
         (.setColor (-> turquoise hex->rgb-int java.awt.Color.))
         (.setFont (java.awt.Font. "GillSans-SemiBold" java.awt.Font/BOLD 22))
         (.drawString "Start making PDFs today!" margin-left 0)
         (.setFont (java.awt.Font. "GillSans-SemiBold" java.awt.Font/PLAIN 14))
         (.drawString
          "Your absolutely useless and way too short guide to the awesome clj-pdf Clojure library."
          margin-left
          20)
         (.setColor java.awt.Color/BLACK)
         (.fillRect -25 35 width 300)
         (.setColor (-> turquoise hex->rgb-int java.awt.Color.))
         (.fillRect -25 335 width 5))))]
   [:graphics {:translate [0 185] :rotate -0.05}
    #(cover-text % page-width)]
   [:svg {:scale [-0.5 0.5]
          :translate [500 600]}
    (io/resource "img/face.svg")]])

(defn draw-string-in-center
 [g2d text y width]
 (.drawString g2d 0 0))

(def title-square
 (let [width 400
       height 200
       x (- (/ page-width 2) (/ width 2))
       y 100]
   [:graphics {:translate [x y]}
    (fn [g2d]
      (doto g2d
        (.fillRect 0 0 width height)
        (cover-text width)))]))


; -- Rendering -----------------------------------------------------------------
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
       :footer {:start-page 2 :align :center}
       :register-system-fonts? true}
      [:pagebreak]
      title-square]
     dest)
    (println "Generated pdf at" dest)))
