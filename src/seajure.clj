(ns seajure
  (:use [net.cgrand.enlive-html :only [deftemplate content set-attr do-> at]]
        [clj.io :only [spit copy file]]))

(def assets ["style.css" "logo.png"])

(defn get-resource-stream [path]
  (.getResourceAsStream (clojure.lang.RT/baseLoader) path))

(defn read-resource [path]
  (-> (get-resource-stream path)
      (java.io.InputStreamReader.)
      (java.io.PushbackReader.)
      (read)))

;; TODO: I'm just happy to get this to output correctly; I'm sure it's
;; very unidiomatic Enlive usage. Fix it once Enlive gets docs. =\
(defn member-project-links [projects]
  (fn [matched]
    (for [p projects]
      (at matched [:a] (do-> (content (name p))
                             (set-attr :href (str "#" p)))))))

(defn member-links [members]
  (fn [matched]
    (for [{:keys [name url projects]} members]
      (at matched
          [:li :a] (do-> (content name)
                         (set-attr :href url))
          [:li :span] (member-project-links projects)))))

(defn project-links [projects]
  (fn [matched]
    (for [[anchor {:keys [name url description]}] projects]
      (at matched
          [:dt :a] (do-> (content name)
                         (set-attr :name anchor)
                         (set-attr :href url))
          [:dd] (content description)))))

(deftemplate index "index.html" [members projects]
  [:ul.members] (member-links members)
  [:dl.projects] (project-links projects))

(defn -main
  ([out]
     (let [members (read-resource "members.clj")
           projects (read-resource "projects.clj")
           lines (index members projects)]
       (doseq [f assets] (copy (get-resource-stream f) (file out f)))
       (spit (file out "index.html") (apply str lines))))
  ([] (-main "public")))
