import csv
import cv2
import keras_ocr
import numpy as np

def create_generator_from_csv(csv_path, img_width=200, img_height=50):
    with open(csv_path, newline='') as csvfile:
        reader = csv.reader(csvfile)
        samples = list(reader)

    def generator():
        while True:
            for image_path, label in samples:
                image = cv2.imread(image_path, cv2.IMREAD_GRAYSCALE)
                image = cv2.resize(image, (img_width, img_height))
                image = np.expand_dims(image, axis=-1)  # add channel dimension
                yield image, label
    return generator()



recognizer = keras_ocr.recognition.Recognizer()
train_gen = create_generator_from_csv('training.csv')

# You may want to limit steps per epoch or set batch size
recognizer.train(
    image_generator=train_gen,
    epochs=10,
    steps_per_epoch=100  # or len(samples) // batch_size
)


recognizer.model.save('total_battle.h5')  