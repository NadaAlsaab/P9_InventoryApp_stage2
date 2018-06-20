package com.example.android.p9;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.example.android.p9.data.BookContracts.booksEntry;

public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_BOOK_LOADER = 0;

    private Uri currentBookUri;

    private TextView bookTextView;
    private TextView suppTextView;
    private TextView quantityTextView;
    private TextView bookPriceText;
    private TextView suppPhoneText;
    private TextView suppEmailText;

    private Button call;
    private Button mail;
    private Button increase;
    private Button decrease;
    private Button delete;

    private boolean isChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);


        Intent intent = getIntent();
        currentBookUri = intent.getData();

        if (currentBookUri != null) {
            setTitle("Book Details");
            getLoaderManager().initLoader(EXISTING_BOOK_LOADER, null, this);

        }
        bookTextView = findViewById(R.id.detail_book_title);
        suppTextView = findViewById(R.id.detail_supplier_name);
        bookPriceText = findViewById(R.id.detail_price);
        quantityTextView = findViewById(R.id.detail_quantity);
        suppPhoneText = findViewById(R.id.detail_phone);
        suppEmailText = findViewById(R.id.detail_mail);

        call = findViewById(R.id.call_button);
        mail = findViewById(R.id.email_button);
        increase = findViewById(R.id.increase_button);
        decrease = findViewById(R.id.decrease_button);
        delete = findViewById(R.id.delete_button);

        increase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                increment();
            }
        });

        decrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                decrement();
            }
        });

        mail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL, new String[]{suppEmailText.getText().toString()});
                i.putExtra(Intent.EXTRA_SUBJECT, "Nachos Shop Request");

                try {
                    startActivity(Intent.createChooser(i, "Send mail..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(DetailsActivity.this, "No email for client", Toast.LENGTH_SHORT).show();
                }

            }
        });

        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + suppPhoneText.getText().toString()));
                startActivity(intent);
            }
        });

        delete.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                showDeleteConfirmationDialog();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem book) {

        switch (book.getItemId()) {
            case R.id.edit:
                Intent intent = new Intent(DetailsActivity.this, EditorActivity.class);
                intent.setData(currentBookUri);
                startActivityForResult(intent, 9);
                return true;

            case R.id.action_save: {
                saveBook();
                finish();
                return true;
            }

            case android.R.id.home:
                if (!isChanged) {
                    NavUtils.navigateUpFromSameTask(DetailsActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(DetailsActivity.this);
                            }
                        };

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(book);
    }

    @Override
    public void onBackPressed() {
        if (!isChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int id) {
                finish();
            }
        };
        showUnsavedChangesDialog(discardButtonClickListener);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                booksEntry._ID,
                booksEntry.COLUMN_BOOKS_NAME,
                booksEntry.COLUMN_SUPPLIER,
                booksEntry.COLUMN_QUANTITY,
                booksEntry.COLUMN_PRICE,
                booksEntry.COLUMN_SUPER_PHONE,
                booksEntry.COLUMN_SUPP_EMAIL,

        };

        return new CursorLoader(this,
                currentBookUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {

            int bookIndex = cursor.getColumnIndex(booksEntry.COLUMN_BOOKS_NAME);
            int supplierIndex = cursor.getColumnIndex(booksEntry.COLUMN_SUPPLIER);
            int quantityIndex = cursor.getColumnIndex(booksEntry.COLUMN_QUANTITY);
            int priceIndex = cursor.getColumnIndex(booksEntry.COLUMN_PRICE);
            int phoneIndex = cursor.getColumnIndex(booksEntry.COLUMN_SUPER_PHONE);
            int mailIndex = cursor.getColumnIndex(booksEntry.COLUMN_SUPP_EMAIL);

            String title = cursor.getString(bookIndex);
            String supplier = cursor.getString(supplierIndex);
            int quant = cursor.getInt(quantityIndex);
            int price = cursor.getInt(priceIndex);
            int phone = cursor.getInt(phoneIndex);
            String email = cursor.getString(mailIndex);

            bookTextView.setText(title);
            suppTextView.setText(supplier);
            quantityTextView.setText(Integer.toString(quant));
            bookPriceText.setText(Integer.toString(price));
            suppPhoneText.setText(Integer.toString(phone));
            suppEmailText.setText(email);

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        bookTextView.setText("");
        suppTextView.setText("");
        quantityTextView.setText("");
        bookPriceText.setText("");
        suppPhoneText.setText("");
        suppEmailText.setText("");
    }

    private void increment() {

        String quantityString = quantityTextView.getText().toString().trim();


        int quantity = Integer.parseInt(quantityString);
        quantity = quantity + 1;

        ContentValues values = new ContentValues();
        values.put(booksEntry.COLUMN_QUANTITY, quantity);

        getContentResolver().update(currentBookUri, values, null, null);
    }

    private void decrement() {
        String quantityString = quantityTextView.getText().toString().trim();
        int quantity = Integer.parseInt(quantityString);

        if (quantity > 0) {
            quantity = quantity - 1;
        } else if (quantity == 0) {
            Toast.makeText(DetailsActivity.this, getString(R.string.less_than_zero),
                    Toast.LENGTH_SHORT).show();
        }

        ContentValues values = new ContentValues();

        values.put(booksEntry.COLUMN_QUANTITY, quantity);
        getContentResolver().update(currentBookUri, values,null, null);
    }


    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.not_saved_assurance);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.continue_, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_book_assurance);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                deleteBook();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteBook() {
        int deletedRows;
        if (currentBookUri != null) {
            deletedRows = getContentResolver().delete(currentBookUri, null, null);

            if (deletedRows == 0) {
                Toast.makeText(this, getString(R.string.failed_deletion),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.successful_deletion),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    private void saveBook() {

        int price;
        int quantity;
        int changedRows;
        int phone;

        String bookString = bookTextView.getText().toString().trim();
        String suppString = suppTextView.getText().toString().trim();
        String priceString = bookPriceText.getText().toString().trim();
        String quantityString = quantityTextView.getText().toString().trim();
        String suppPhoneString = suppPhoneText.getText().toString().trim();
        String suppEmailString = suppEmailText.getText().toString().trim();

        if (currentBookUri == null && TextUtils.isEmpty(bookString) && TextUtils.isEmpty(suppString)) {
            Toast.makeText(this, getString(R.string.info_correction_check), Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(bookString)) {
            Toast.makeText(this, getString(R.string.info_correction_check), Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(suppEmailString)) {
            Toast.makeText(this, getString(R.string.info_correction_check), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!TextUtils.isEmpty(priceString) && TextUtils.isDigitsOnly(priceString) ) {
            price = Integer.parseInt(priceString);
        } else {
            Toast.makeText(this, getString(R.string.info_correction_check), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!TextUtils.isEmpty(quantityString) && TextUtils.isDigitsOnly(quantityString) ) {
            quantity = Integer.parseInt(quantityString);
        } else {
            Toast.makeText(this, getString(R.string.info_correction_check), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!TextUtils.isEmpty(suppPhoneString)) {
            phone = Integer.parseInt(suppPhoneString);
        } else {
            Toast.makeText(this, getString(R.string.info_correction_check),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(booksEntry.COLUMN_BOOKS_NAME, bookString);
        contentValues.put(booksEntry.COLUMN_SUPPLIER, suppString);
        contentValues.put(booksEntry.COLUMN_PRICE, priceString);
        contentValues.put(booksEntry.COLUMN_QUANTITY, quantityString);
        contentValues.put(booksEntry.COLUMN_SUPP_EMAIL, suppEmailString);
        contentValues.put(booksEntry.COLUMN_PRICE, price);
        contentValues.put(booksEntry.COLUMN_QUANTITY, quantity);
        contentValues.put(booksEntry.COLUMN_SUPER_PHONE, phone);

        if (currentBookUri == null) {

            Uri newUri = getContentResolver().insert(booksEntry.CONTENT_URI, contentValues);
            if (newUri == null) {
                Toast.makeText(this, getString(R.string.failed_insertion),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.successful_insertion),
                        Toast.LENGTH_SHORT).show();
            }
        } else {

            changedRows = getContentResolver().update(currentBookUri, contentValues, null, null);
            if (changedRows == 0) {
                Toast.makeText(this, getString(R.string.failed_updating),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.successful_updating),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
