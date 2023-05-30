from keras.preprocessing.image import ImageDataGenerator
import tensorflow as tf
from keras.models import Sequential
from keras.layers import Dense, Dropout, Flatten
from keras.layers import Conv2D, MaxPooling2D, Lambda
from keras.preprocessing.image import ImageDataGenerator
from keras.models import load_model
import matplotlib.pyplot as plt


class NeuralUvp:

    def __init__(self, train_generator, validation_generator):
        classes = ['Baroque', 'Classicism', 'Deconstructivism', 'Deconstructivism_2', 'Gothic', 'Modernism']
        model = tf.keras.models.Sequential([
            tf.keras.layers.Conv2D(16, (3, 3), input_shape=(250, 250, 3), padding="same", activation="relu"),
            tf.keras.layers.MaxPooling2D((2, 2)),

            tf.keras.layers.Conv2D(32, (3, 3), padding="same", activation="relu"),
            tf.keras.layers.MaxPooling2D((2, 2)),

            tf.keras.layers.Conv2D(64, (3, 3), padding="same", activation="relu"),
            tf.keras.layers.MaxPooling2D((2, 2)),
            tf.keras.layers.Dropout(0.2),

            tf.keras.layers.Conv2D(128, (3, 3), padding="same", activation="relu"),
            tf.keras.layers.MaxPooling2D((2, 2)),
            tf.keras.layers.Dropout(0.2),

            tf.keras.layers.Conv2D(256, (3, 3), padding="same", activation="relu"),
            tf.keras.layers.MaxPooling2D((2, 2)),
            tf.keras.layers.Dropout(0.2),

            tf.keras.layers.Flatten(),
            tf.keras.layers.Dense(512, activation="relu"),
            tf.keras.layers.Dense(256, activation="relu"),
            tf.keras.layers.Dense(len(classes), activation="softmax")
        ])

        model.compile(loss='categorical_crossentropy', optimizer='adam', metrics=['accuracy'])

        graphics = model.fit(
            train_generator,
            steps_per_epoch=train_generator.samples / train_generator.batch_size,
            epochs=35,
            validation_data=validation_generator,
            validation_steps=validation_generator.samples / validation_generator.batch_size)

        model.save('112>111.h5')

        acc = graphics.history['accuracy']
        val_acc = graphics.history['val_accuracy']
        loss = graphics.history['loss']
        val_loss = graphics.history['val_loss']
        epochs = range(1, len(acc) + 1)

        plt.plot(epochs, acc, 'b', label='Training accuracy')
        plt.plot(epochs, val_acc, 'c', label='Validation accuracy')
        plt.title('Training and validation accuracy')
        plt.xlabel('Epochs')
        plt.ylabel('Accuracy')
        plt.legend()

        plt.show()

        plt.plot(epochs, loss, 'c', label='Training loss')
        plt.plot(epochs, val_loss, 'b', label='Validation loss')
        plt.title('Training and validation loss')
        plt.xlabel('Epochs')
        plt.ylabel('Loss')
        plt.legend()

        plt.show()
